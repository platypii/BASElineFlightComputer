package com.platypii.baseline.location;

import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.util.Numbers;

import android.content.Context;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.os.Build;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

class LocationProviderNMEA extends LocationProvider implements GpsStatus.NmeaListener {
    protected final String TAG = "ProviderNMEA";
    private static final String NMEA_TAG = "NMEA";

    private final MyAltimeter alti;

    // Most recent data
    private long lastFixMillis = -1;
    private double latitude = Double.NaN;
    private double longitude = Double.NaN;
    private double altitude_gps = Double.NaN;
    private double vN = Double.NaN;
    private double vE = Double.NaN;
    // private double groundSpeed = Double.NaN;
    // private double bearing = Double.NaN;
    private float pdop = Float.NaN;
    private float hdop = Float.NaN;
    private float vdop = Float.NaN;
    private long dateTime; // The number of milliseconds until the start of this day, midnight GMT

    // Satellite data
    private int satellitesInView = -1;
    private int satellitesUsed = -1;

    // Android Location manager
    @Nullable
    private static LocationManager manager;

    @NonNull
    @Override
    protected String providerName() {
        return TAG;
    }

    @NonNull
    @Override
    protected String dataSource() {
        return "NMEA " + Build.MANUFACTURER + " " + Build.MODEL;
    }

    LocationProviderNMEA(MyAltimeter alti) {
        this.alti = alti;
    }

    /**
     * Start location updates
     *
     * @param context The Application context
     */
    @Override
    public void start(@NonNull Context context) throws SecurityException {
        // Start NMEA updates
        manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (manager != null) {
            try {
                final boolean nmeaSuccess = manager.addNmeaListener(this);
                if (!nmeaSuccess) {
                    Log.e(TAG, "Failed to start NMEA updates");
                }
            } catch (SecurityException e) {
                Log.e(TAG, "Failed to start NMEA location service, permission denied");
            }
        } else {
            Log.e(TAG, "failed to get android location manager");
        }
    }

    private void updateLocation() {
        updateLocation(new MLocation(
                lastFixMillis, latitude, longitude, altitude_gps, alti.climb, vN, vE,
                Float.NaN, pdop, hdop, vdop, satellitesUsed, satellitesInView
        ));
    }

    /**
     * This is the main NMEA parsing function.
     * NMEA strings are trimmed, validated, and then parsed into NMEA commands.
     * Location and velocity data is set as NMEA commands arrive.
     * Location is officially updated when we receive the RMC "recommended minimum data" command.
     *
     * @param timestamp milliseconds
     * @param nmea the NMEA string
     */
    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        // Log.v(NMEA_TAG, "[" + timestamp + "] " + nmea.trim()); // Trim because logcat fails on trailing \0
        if (nmea.charAt(0) == '$') {
            handleNmea(timestamp, nmea);
        } else {
            Log.e(NMEA_TAG, "Failed to parse " + nmea.trim());
        }
    }

    private void handleNmea(long timestamp, String nmea) {
        nmea = NMEA.cleanNmea(nmea);
        if (nmea.length() < 8) {
            return;
        }

        // Check for missing line breaks
        if (nmea.indexOf('$', 1) > 0) {
            Log.w(TAG, "Splitting multiple NMEA sentences: " + nmea);
            final String[] split = nmea.split("\\$");
            // Recurse on split sentences
            for (String str : split) {
                if (!str.isEmpty()) {
                    onNmeaReceived(timestamp, "$" + str);
                }
            }
        }

        try {
            // Validate NMEA sentence, ignore invalid
            if (NMEA.validate(nmea)) {
                parseNmea(nmea);
            }
        } catch (Exception e) {
            Exceptions.report(new NMEAException("Exception while handling NMEA: " + nmea, e));
        }
    }

    private void parseNmea(@NonNull String nmea) throws NMEAException {
        // Parse NMEA command
        final String[] split = NMEA.splitNmea(nmea);
        final String command = split[0].substring(3);
        switch (command) {
            case "GGA":
                if (split.length < 11) {
                    throw new NMEAException("Invalid GGA command");
                }

                // Fix data
                // latitude = NMEA.parseDegreesMinutes(split[2], split[3]);
                // longitude = NMEA.parseDegreesMinutes(split[4], split[5]);
                // gpsFix = Numbers.parseInt(split[6], -1); // 0 = Invalid, 1 = Valid SPS, 2 = Valid DGPS, 3 = Valid PPS
                satellitesUsed = Numbers.parseInt(split[7], -1);
                hdop = Numbers.parseFloat(split[8]);
                if (!split[9].isEmpty() && split[10].equals("M")) {
                    altitude_gps = Numbers.parseDouble(split[9]);
                }
                // double geoidSeparation = parseDouble(split[11]]); // Geoid separation according to WGS-84 ellipsoid
                // assert split[12].equals("M")// Separation Units
                // double dgpsAge = parseDouble(split[13]); // Age of Differential GPS Data (secs)
                // int dgpsStationId = parseDouble(split[14]); // Differential Reference Station ID
                // TODO: hAcc, vAcc, sAcc
                // Time. At this point we have (at least) 3 choices of clock.
                // 1) timestamp parameter, is measured in milliseconds
                // 2) System.currentTime(), depends on how long execution takes to this point
                // 3) GPS time, most accurate, but must be parsed carefully, since we only get milliseconds since midnight GMT
                // split[1]: Time: 123456 = 12:34:56 UTC
                // if (dateTime == 0 || split[1].isEmpty())
                //   lastFixMillis = timestamp; // Alt: System.currentTimeMillis();
                // else
                //   lastFixMillis = dateTime + parseTime(split[1]);

                break;
            case "RMC":
                // Recommended minimum data for gps
                // This is the NMEA command that we use as the "keyframe" of the NMEA stream.
                // When we receive a valid RMC command, we issue an updateLocation() to listeners.

                if (split.length < 10) {
                    throw new NMEAException("Invalid RMC command");
                }

                // boolean status = split[2].equals("A"); // A = active, V = void
                latitude = NMEA.parseDegreesMinutes(split[3], split[4]);
                longitude = NMEA.parseDegreesMinutes(split[5], split[6]);
                final double groundSpeedRMC = Convert.kts2mps(Numbers.parseDouble(split[7])); // Speed over ground
                final double bearingRMC = Numbers.parseDouble(split[8]); // Course over ground
                // split[10], split[11]: 003.1,W magnetic variation
                // split[9]: Date: 230394 = 23 March 1994
                // split[1]: Time: 123456 = 12:34:56 UTC
                dateTime = NMEA.parseDate(split[9]);
                lastFixMillis = dateTime + NMEA.parseTime(split[1]);
                // Log.w("Time", "["+timestamp+"] lastFixMillis = " + lastFixMillis + ", currentTime = " + System.currentTimeMillis());

                // Computed parameters
                vN = groundSpeedRMC * Math.cos(Math.toRadians(bearingRMC));
                vE = groundSpeedRMC * Math.sin(Math.toRadians(bearingRMC));

                // Log.i(NMEA_TAG, "["+time+"] " + Convert.latlong(latitude, longitude) + ", groundSpeed = " + Convert.speed(groundSpeed) + ", bearing = " + Convert.bearing2(bearing));

                // Sanity checks
                final int locationError = LocationCheck.validate(latitude, longitude);
                if (locationError == LocationCheck.VALID) {
                    // Warnings
                    if (lastFixMillis <= 0) {
                        Log.w(NMEA_TAG, "Invalid timestamp " + lastFixMillis + ", nmea: " + nmea);
                    }
                    // Update the official location!
                    updateLocation();
                } else {
                    Log.w(NMEA_TAG, LocationCheck.message[locationError] + ": " + latitude + "," + longitude);
                }
                break;
            case "GNS":
                // Fixes data for single or combined (GPS, GLONASS, etc) satellite navigation systems
                lastFixMillis = dateTime + NMEA.parseTime(split[1]);
                // latitude = NMEA.parseDegreesMinutes(split[2], split[3]);
                // longitude = NMEA.parseDegreesMinutes(split[4], split[5]);
                // modeIndicator = split[6]
                if (!split[7].isEmpty()) {
                    satellitesUsed = Integer.parseInt(split[7]);
                }
                // hdop = Numbers.parseFloat(split[8]);
                if (!split[9].isEmpty()) {
                    altitude_gps = Numbers.parseDouble(split[9]);
                    // double geoidSeparation = parseDouble(split[10]]);
                }
                break;
            case "GSA":
                // Overall satellite data (DOP and active satellites)
                // boolean autoDim = split[1].equals("A"); // A = Auto 2D/3D, M = Forced 2D/3D
                // gpsFix = split[2].isEmpty() ? 0 : Integer.parseInt(split[2]); // 0 = null, 1 = No fix, 2 = 2D, 3 = 3D
                pdop = Numbers.parseFloat(split[15]);
                hdop = Numbers.parseFloat(split[16]);
                vdop = Numbers.parseFloat(split[17]);
                break;
            case "GSV":
                // Detailed satellite data (satellites in view)
                satellitesInView = Numbers.parseInt(split[3], -1);
                break;
            case "PWR":
                // Dual proprietary sentence for power, handled in LocationProviderBluetooth
            case "VTG":
                // final double bearingVTG = Numbers.parseDouble(split[1]); // Course over ground
                // final double groundSpeedVTG = Convert.kts2mps(Numbers.parseDouble(split[5])); // Speed over ground
            case "GLL":
                // latitude = parseDegreesMinutes(split[1], split[2]);
                // longitude = parseDegreesMinutes(split[3], split[4]);
                // long time = parseUTC(split[5]); // 123456 = 12:34:56 UTC
                // boolean status = split[6].equals("A"); // A = active, V = void
            case "ACC":
            case "ACCURACY":
                // $GNACCURACY,0.8*1E
            case "ATT":
                // $GPATT,45.781233,10.862333,1796.3,45.0,2.6,2.6,*72
                // $GPATT,lat,lon,alt,bear?,???,???
            case "GLG":
                // $GPGLG,VER2,GNSS,130616,171913.0,85,4737.550964,N,12219.566617,W,96.0,38.5,2.0,0.0,13.0,TP,Seoul,,,15,3,,*5C
                // $GPGLG,version,provider,???,???,???,lat,lat,lon,lon,...
            case "GRS":
                // Bluetooth:
                // $GNGRS,153238.10,1,2.7,0.1,-8.4,-18.5,8.6,2.1,12.3,,,,,*78
            case "GST":
                // $GNGST,183132.000,6.1,15,11,76,11,15,32
                // split[2]: RMS value of the standard deviation of the range inputs to the navigation process. Range inputs include preudoranges & DGNSS corrections.
                // split[3]: Stdev of semi-major axis of error ellipse (meters)
                // split[4]: Stdev of semi-minor axis of error ellipse (meters)
                // split[5]: Orientation of semi-major axis of error ellipse (degrees from true north)
                // split[6]: Stdev of latitude error (meters)
                // split[7]: Stdev of longitude error (meters)
                // split[8]: Stdev of altitude error (meters)
                break;
            default:
                // Log.d(TAG, "Unknown NMEA command " + command + ": " + nmea);
        }
    }

    @Override
    public void stop() {
        super.stop();
        if (manager != null) {
            manager.removeNmeaListener(this);
            manager = null;
        }
    }
}
