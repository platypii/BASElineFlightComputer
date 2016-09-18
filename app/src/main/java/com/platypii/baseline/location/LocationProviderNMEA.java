package com.platypii.baseline.location;

import android.content.Context;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.util.Log;
import com.platypii.baseline.bluetooth.BluetoothService;
import com.google.firebase.crash.FirebaseCrash;
import com.platypii.baseline.data.measurements.MLocation;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Util;

class LocationProviderNMEA extends LocationProvider implements GpsStatus.NmeaListener {
    protected final String TAG = "LocationServiceNMEA";
    private static final String NMEA_TAG = "NMEA";

    // SkyPro GPS sends the following bytes when connection is initialized
    private static final String skyproPrefix = new String(new byte[] {
            85,4,0,56,0,0,-17,-65,-67,85,4,0,56,0,0,-17,-65,-67,85,4,0,56,0,0,-17,-65,-67
    });

    public boolean nmeaReceived = false;

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
    private int gpsFix;

    // Android Location manager
    private static LocationManager manager;

    @Override
    protected String providerName() {
        return TAG;
    }

    /**
     * Start location updates
     * @param context The Application context
     */
    @Override
    public synchronized void start(@NonNull Context context) throws SecurityException {
        // Start NMEA updates
        manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        final boolean nmeaSuccess = manager.addNmeaListener(this);
        if (!nmeaSuccess) {
            Log.e(TAG, "Failed to start NMEA updates");
        }
    }

    private void updateLocation() {
        final float hAcc = Float.NaN;
        super.updateLocation(new MLocation(
                lastFixMillis, latitude, longitude, altitude_gps, vN, vE,
                hAcc, pdop, hdop, vdop, satellitesUsed
        ));
    }

    /**
     * This is the main NMEA parsing function.
     * NMEA strings are trimmed, validated, and then parsed into NMEA commands.
     * Location and velocity data is set as NMEA commands arrive.
     * Location is officially updated when we receive the RMC "recommended minimum data" command.
     * @param timestamp milliseconds
     * @param nmea the NMEA string
     */
    @Override
    public void onNmeaReceived(long timestamp, String nmea) {
        nmea = nmea.trim();
        // Log.v(NMEA_TAG, "[" + timestamp + "] " + nmea);

        if (!nmeaReceived) {
            Log.d(NMEA_TAG, "First NMEA string received");
            nmeaReceived = true;
        }

        try {
            handleNmea(timestamp, nmea);
        } catch(Exception e) {
            Log.e(NMEA_TAG, "Exception while handling NMEA: " + nmea, e);
            FirebaseCrash.report(e);
        }
    }

    private void handleNmea(long timestamp, String nmea) {
        // Remove skypro welcome message
        if(BluetoothService.preferenceEnabled && nmea.startsWith(skyproPrefix)) {
            nmea = nmea.substring(skyproPrefix.length());
        }

        // Validate NMEA sentence and print errors, but still try to parse
        NMEA.validate(nmea);

        final String split[] = nmea.split(",");
        final String command = split[0].substring(3);

        // Parse command
        switch (command) {
            case "GSA":
                // Overall satellite data (DOP and active satellites)
                // boolean autoDim = split[1].equals("A"); // A = Auto 2D/3D, M = Forced 2D/3D
                // gpsFix = split[2].isEmpty() ? 0 : Integer.parseInt(split[2]); // 0 = null, 1 = No fix, 2 = 2D, 3 = 3D
                pdop = Util.parseFloat(split[split.length - 4]);
                hdop = Util.parseFloat(split[split.length - 3]);
                vdop = Util.parseFloat(split[split.length - 2]);
                break;
            case "GSV":
                // Detailed satellite data (satellites in view)
                satellitesInView = parseInt(split[3]);
                break;
            case "GGA":
                // Fix data
                latitude = NMEA.parseDegreesMinutes(split[2], split[3]);
                longitude = NMEA.parseDegreesMinutes(split[4], split[5]);
                gpsFix = parseInt(split[6]); // 0 = Invalid, 1 = Valid SPS, 2 = Valid DGPS, 3 = Valid PPS
                satellitesUsed = parseInt(split[7]);
                hdop = Util.parseFloat(split[8]);
                if (!split[9].isEmpty()) {
                    if(!split[10].equals("M")) {
                        Log.e(NMEA_TAG, "Expected meters, was " + split[10] + " in nmea: " + nmea);
                        FirebaseCrash.report(new NMEAException("Expected meters, was " + split[10] + " in nmea: " + nmea));
                    }
                    altitude_gps = Util.parseDouble(split[9]);
                }
                // double geoidSeparation = parseDouble(split[11]]); // Geoid separation according to WGS-84 ellipsoid
                // assert split[12].equals("M")// Separation Units
                // double dgpsAge = parseDouble(split[13]);
                // int dgpsStationId = parseDouble(split[14]);
                // TODO: hAcc, vAcc, sAcc
                // Time. At this point we have (at least) 3 choices of clock.
                // 1) timestamp parameter, is measured in milliseconds
                // 2) System.currentTime(), depends on how long execution takes to this point
                // 3) GPS time, most accurate, but must be parsed carefully, since we only get milliseconds since midnight GMT
                // split[1]: Time: 123456 = 12:34:56 UTC
                // if(dateTime == 0 || split[1].isEmpty())
                //   lastFixMillis = timestamp; // Alt: System.currentTimeMillis();
                // else
                //   lastFixMillis = dateTime + parseTime(split[1]);

                break;
            case "RMC":
                // Recommended minimum data for gps
                // This is the NMEA command that we use as the "keyframe" of the NMEA stream.
                // When we receive a valid RMC command, we issue an updateLocation() to listeners.

                // boolean status = split[2].equals("A"); // A = active, V = void
                latitude = NMEA.parseDegreesMinutes(split[3], split[4]);
                longitude = NMEA.parseDegreesMinutes(split[5], split[6]);
                final double groundSpeedRMC = Convert.kts2mps(Util.parseDouble(split[7])); // Speed over ground
                final double bearingRMC = Util.parseDouble(split[8]); // Course over ground
                // split[10], split[11]: 003.1,W magnetic Variation
                // split[9]: Date: 230394 = 23 March 1994
                // split[1]: Time: 123456 = 12:34:56 UTC
                dateTime = NMEA.parseDate(split[9]);
                lastFixMillis = dateTime + NMEA.parseTime(split[1]);
                // Log.w("Time", "["+timestamp+"] lastFixMillis = " + lastFixMillis + ", currentTime = " + System.currentTimeMillis());
                if(gpsFix == 0) {
                    // Log.v(NMEA_TAG, "Invalid fix, nmea: " + nmea);
                } else if(lastFixMillis <= 0) {
                    Log.w(NMEA_TAG, "Invalid timestamp " + lastFixMillis + ", nmea: " + nmea);
                }

                // Computed parameters
                vN = groundSpeedRMC * Math.cos(Math.toRadians(bearingRMC));
                vE = groundSpeedRMC * Math.sin(Math.toRadians(bearingRMC));

                // Log.i(NMEA_TAG, "["+time+"] " + Convert.latlong(latitude, longitude) + ", groundSpeed = " + Convert.speed(groundSpeed) + ", bearing = " + Convert.bearing2(bearing));

                if(Util.isReal(latitude) && Util.isReal(longitude)) {
                    final double latitude_abs = Math.abs(latitude);
                    final double longitude_abs = Math.abs(longitude);
                    if(latitude_abs < 0.1 && longitude_abs < 0.1) {
                        // If lat,lon == 0,0 assume bad data (there's no BASE off the coast of Africa)
                        Log.e(NMEA_TAG, "RMC unlikely lat/long: " + nmea);
                        FirebaseCrash.report(new NMEAException("RMC unlikely lat/long: " + nmea));
                    } else if(latitude_abs > 180.0 || longitude_abs > 180.0) {
                        // Lat/lon out of bounds. Likely parsing error.
                        Log.e(NMEA_TAG, "RMC lat/long out of bounds: " + nmea);
                        FirebaseCrash.report(new NMEAException("RMC lat/long out of bounds: " + nmea));
                    } else {
                        // If lat or lon == 0, give a warning, but still update location
                        if(latitude_abs < 0.1) {
                            Log.w(NMEA_TAG, "RMC unlikely latitude: " + nmea);
                            FirebaseCrash.report(new NMEAException("RMC unlikely latitude: " + nmea));
                        } else if(longitude_abs < 0.1) {
                            Log.w(NMEA_TAG, "RMC unlikely longitude: " + nmea);
                            FirebaseCrash.report(new NMEAException("RMC unlikely longitude: " + nmea));
                        }
                        // Update the official location!
                        updateLocation();
                    }
                }
                break;
            case "GNS":
                // Fixes data for single or combined (GPS, GLONASS, etc) satellite navigation systems
                lastFixMillis = dateTime + NMEA.parseTime(split[1]);
                latitude = NMEA.parseDegreesMinutes(split[2], split[3]);
                longitude = NMEA.parseDegreesMinutes(split[4], split[5]);
                if (!split[9].isEmpty()) {
                    altitude_gps = Util.parseDouble(split[9]);
                    // double geoidSeparation = parseDouble(split[10]]);
                }
                if (!split[7].isEmpty()) {
                    satellitesUsed = Integer.parseInt(split[7]);
                }
                break;
            case "VTG":
                // final double bearingVTG = Util.parseDouble(split[1]); // Course over ground
                // final double groundSpeedVTG = Convert.kts2mps(Util.parseDouble(split[5])); // Speed over ground
                break;
            case "GLL":
                // latitude = parseDegreesMinutes(split[1], split[2]);
                // longitude = parseDegreesMinutes(split[3], split[4]);
                // long time = parseUTC(split[5]); // 123456 = 12:34:56 UTC
                // boolean status = split[6].equals("A"); // A = active, V = void
                break;
            case "ACC":
            case "ACCURACY":
                // $GNACCURACY,0.8*1E
                break;
            case "ATT":
                // $GPATT,45.781233,10.862333,1796.3,45.0,2.6,2.6,*72
                // $GPATT,lat,lon,alt,bear?,???,???
                break;
            case "GLG":
                // $GPGLG,VER2,GNSS,130616,171913.0,85,4737.550964,N,12219.566617,W,96.0,38.5,2.0,0.0,13.0,TP,Seoul,,,15,3,,*5C
                // $GPGLG,version,provider,???,???,???,lat,lat,lon,lon,...
                break;
            case "LOR":
                // Samsung Galaxy S6 SM-G920F
                // $PGLOR,0,HLA,123444.00,L,,Al,,A,,H,,,M,1,Ac,0,Gr,0,S,,,Sx,,,T,0,Tr,,Mn,0*33
                // $PGLOR,1,FIX,1.0,1.0*20
                // $PGLOR,2,SIO,TxERR,0,RxERR,0,TxCNT,461,RxCNT,4416,MLFRMPKT,0,DTMS,997,DTIN,5,11,DTOUT,100,987,HATMD,26*20
                // $PGLOR,2,SAT,3,3,G17,033,1F,R24,020,17,G29,026,1F*2F
                // $PGLOR,3,PWR,mA,36.4,RFTm,1000,OscTm,1000,MeasTm,1000,UTC,123444.00*32
                // $PGLOR,6,STA,122301.03,0.000,0.000,-270,236,9999,0,P,F,L,1,C,0,S,0000,0,2,R,0000,TPeF,19,2122,LC,,*13
                // $PGLOR,SPL,20160704142257.8,STATUS,2*38
                break;
            case "TIS":
                // Samsung Note7
                // $PSTIS,*61
                break;
            case "ZDA":
                // $GPZDA,115729.61,18,06,2016,,*62
                break;
            case "PWR":
                // I don't know what PWR does, but we get a lot of them via bluetooth
                break;
            default:
                Log.w(NMEA_TAG, "[" + timestamp + "] Unknown NMEA command: " + nmea);
                FirebaseCrash.report(new NMEAException("Unknown NMEA command " + command + ": " + nmea));
        }
    }

    private static int parseInt(String str) {
        return str.isEmpty() ? -1 : Integer.parseInt(str);
    }

    @Override
    public void stop() {
        super.stop();
        if(manager != null) {
            manager.removeNmeaListener(this);
            manager = null;
        }
    }
}
