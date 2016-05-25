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
     public void onNmeaReceived(long timestamp, String nmea) {
         nmea = nmea.trim();

         // Log.v(NMEA_TAG, "[" + timestamp + "] " + nmea);

         // Remove skypro welcome message
         if(BluetoothService.preferenceEnabled && nmea.startsWith(skyproPrefix)) {
             nmea = nmea.substring(skyproPrefix.length());
         }

         if (!nmeaReceived) {
             Log.d(NMEA_TAG, "First NMEA string received");
             nmeaReceived = true;
         }

         final String split[] = nmea.split("[,*]");
         final String command = split[0].substring(3, 6);

         // Validate NMEA sentence and print errors, but still try to parse
         NMEA.validate(nmea);

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
                         FirebaseCrash.report(new Exception("Expected meters, was " + split[10] + " in nmea: " + nmea));
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

                 if(Util.isReal(latitude) || Util.isReal(longitude) || Util.isReal(vN) || Util.isReal(vE)) {
                     if(!Util.isReal(latitude) || !Util.isReal(longitude)) {
                         Log.e(NMEA_TAG, "RMC invalid lat/long: " + nmea);
                     }
                     if(Math.abs(latitude) < 0.1 && Math.abs(longitude) < 0.1) {
                         Log.e(NMEA_TAG, "RMC unlikely lat/long: " + nmea);
                     }

                     // Update the official location!
                     updateLocation();
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
             // case "GLL":
             //     // Lat/Long data
             //     latitude = parseDegreesMinutes(split[1], split[2]);
             //     longitude = parseDegreesMinutes(split[3], split[4]);
             //     long time = parseUTC(split[5]); // 123456 = 12:34:56 UTC
             //     boolean status = split[6].equals("A"); // A = active, V = void
             //     break;
             case "PWR":
                 // I don't know what PWR does, but we get a lot of them via bluetooth
                 // Log.v(NMEA_TAG, "[" + timestamp + "] Unknown NMEA command: " + nmea);
                 break;
             default:
                 Log.w(NMEA_TAG, "[" + timestamp + "] Unknown NMEA command: " + nmea);
                 FirebaseCrash.report(new Exception("Unknown NMEA command " + command + ": " + nmea));
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
