package com.platypii.baseline.data;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.location.GpsSatellite;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.GpsStatus;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.platypii.baseline.data.measurements.MLocation;
import com.platypii.baseline.util.Util;

public class MyLocationManager {
    private static final String TAG = "MyLocationManager";
    private static final String NMEA_TAG = "NMEA";

    // Singleton GPSManager
    private static MyLocationManager _instance;

    // Android Location manager
    private static LocationManager manager;

    // Listeners
    private static final List<MyLocationListener> listeners = new ArrayList<>();

    // GPS status
    public static float refreshRate = 0; // Moving average of refresh rate in Hz
    private static boolean nmeaReceived = false;

    // Satellite data
    public static int satellitesInView = -1; // Satellites in view
    public static int satellitesUsed = -1; // Satellites used in last fix

    // Most recent data
    public static long lastFixMillis = -1;
    private static double latitude = Double.NaN;
    private static double longitude = Double.NaN;
    private static double altitude_gps = Double.NaN;
    private static double vN = Double.NaN;
    private static double vE = Double.NaN;
    private static double groundSpeed = Double.NaN;
    private static double bearing = Double.NaN;
    public static float hAcc = Float.NaN;
    //private static float vAcc = Float.NaN;
    //private static float sAcc = Float.NaN;
    private static float pdop = Float.NaN;
    private static float hdop = Float.NaN;
    private static float vdop = Float.NaN;
    private static long dateTime; // The number of milliseconds until the start of this day, midnight GMT
    private static int gpsFix;

    // phone time = GPS time + offset
    public static long phoneOffsetMillis = 0;

    // Computed parameters
    public static float groundDistance = 0;

    // History
    public static MLocation lastLoc; // last location received
    public static MLocation prevLoc; // 2nd to last

    public static final SyncedList<MLocation> history = new SyncedList<>();

    /**
     * Returns the number of milliseconds since the last fix
     */
    public static long lastFixDuration() {
        return System.currentTimeMillis() - (lastFixMillis + phoneOffsetMillis);
    }

    /**
     * Initializes location services
     * @param context The Application context
     */
    public static synchronized void start(@NonNull Context context) throws SecurityException {
        if (_instance == null) {
            _instance = new MyLocationManager();

            // Obtain GPS locations
            manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, androidLocationListener);
            final boolean nmeaSuccess = manager.addNmeaListener(nmeaListener);
            manager.addGpsStatusListener(statusListener);

            if (!nmeaSuccess) {
                Log.e(TAG, "Failed to start NMEA updates");
            }

            // TODO: Start an interval timer to update when signal is lost

        } else {
            Log.w(TAG, "MyLocationManager initialized twice");
        }
    }

    /**
     * Add a new listener to be notified of location updates
     */
    public static void addListener(MyLocationListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener from location updates
     */
    public static void removeListener(MyLocationListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    // This is where we package up all the location data, build a MyLocation, and notify our friends.
    private static final Location tempLoc1 = new Location("gps"); // Temporary android location
    private static final Location tempLoc2 = new Location("gps");

    private static void updateLocation() {

        // Store location
        prevLoc = lastLoc;
        lastLoc = new MLocation(lastFixMillis, latitude, longitude, altitude_gps, vN, vE,
                                hAcc, pdop, hdop, vdop, satellitesUsed, groundDistance);

        // Log.v(TAG, "MyLocationManager.updateLocation(" + lastLoc + ")");

        if (prevLoc != null) {
            // Compute distance
            tempLoc1.setLatitude(prevLoc.latitude);
            tempLoc1.setLongitude(prevLoc.longitude);
            tempLoc2.setLatitude(lastLoc.latitude);
            tempLoc2.setLongitude(lastLoc.longitude);
            groundDistance += tempLoc1.distanceTo(tempLoc2);

            // GPS sample refresh rate
            // TODO: Include time from last sample until now
            final long deltaTime = lastLoc.millis - prevLoc.millis; // time since last refresh
            if (deltaTime > 0) {
                final float refreshTime = 1000.0f / (float) (deltaTime);
                refreshRate += (refreshTime - refreshRate) * 0.5f; // Moving average
                if (Double.isNaN(refreshRate)) {
                    Log.e(TAG, "Refresh rate is NaN, deltaTime = " + deltaTime + " refreshTime = " + refreshTime);
                    refreshRate = 0;
                }
            }
        }

        // Update gps time offset
        phoneOffsetMillis = System.currentTimeMillis() - lastFixMillis;

        // Clear out old values
        resetValues();

        // History
        history.append(lastLoc);

        // Notify listeners (using AsyncTask so the manager never blocks!)
        new AsyncTask<MLocation, Void, Void>() {
            @Override
            protected Void doInBackground(MLocation... params) {
                synchronized (listeners) {
                    for (MyLocationListener listener : listeners) {
                        listener.onLocationChanged(params[0]);
                    }
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                for(MyLocationListener listener : listeners) {
                    listener.onLocationChangedPostExecute();
                }
            }
        }.execute(lastLoc);
    }

    /**
     * Reset lat, long, speed, etc to NaN
     */
    private static void resetValues() {
        latitude = Double.NaN;
        longitude = Double.NaN;
        altitude_gps = Double.NaN;
        vN = Double.NaN;
        vE = Double.NaN;
        groundSpeed = Double.NaN;
        bearing = Double.NaN;
        hAcc = Float.NaN;
        // vAcc = Float.NaN;
        // sAcc = Float.NaN;
        pdop = Float.NaN;
        hdop = Float.NaN;
        vdop = Float.NaN;
        // satellitesInView = -1; // Satellites in view
        // satellitesUsed = -1; // Satellites used in last fix
    }

    /**
     * NMEA listener
     */
    private static final GpsStatus.NmeaListener nmeaListener = new GpsStatus.NmeaListener() {
        // timestamp is milliseconds
        public void onNmeaReceived(long timestamp, String nmea) {
            nmea = nmea.trim();

            // Log.v(NMEA_TAG, "[" + timestamp + "] " + nmea);

            if (!nmeaReceived) {
                Log.d(NMEA_TAG, "First NMEA string received");
                nmeaReceived = true;
            }

            final String split[] = nmea.split("[,*]");
            final String command = split[0].substring(3, 6);

            // Sanity checks
            if (split[0].charAt(0) != '$' || split[0].length() != 6) {
                Log.e(NMEA_TAG, "Invalid NMEA tag: " + split[0]);
            }
            if (!NMEA.nmeaChecksum(nmea)) {
                Log.e(NMEA_TAG, "Invalid NMEA checksum: " + nmea);
            }

            // Parse command
            switch (command) {
                case "GSA":
                    // Overall satellite data (DOP and active satellites)
                    // boolean autoDim = split[1].equals("A"); // A = Auto 2D/3D, M = Forced 2D/3D
                    // gpsFix = split[2].equals("")? 0 : Integer.parseInt(split[2]); // 0 = null, 1 = No fix, 2 = 2D, 3 = 3D
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
                    if (!split[9].equals("")) {
                        if(!split[10].equals("M")) {
                            Log.e(NMEA_TAG, "Expected meters, was " + split[10] + " in nmea: " + nmea);
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
                    // if(dateTime == 0 || split[1].equals(""))
                    //   lastFixMillis = timestamp; // Alt: System.currentTimeMillis();
                    // else
                    //   lastFixMillis = dateTime + parseTime(split[1]);

                    // Update the official location!
                    // MyLocationManager.updateLocation();
                    break;
                case "RMC":
                    // Recommended minimum data for gps
                    // boolean status = split[2].equals("A"); // A = active, V = void
                    latitude = NMEA.parseDegreesMinutes(split[3], split[4]);
                    longitude = NMEA.parseDegreesMinutes(split[5], split[6]);
                    groundSpeed = Convert.kts2mps(Util.parseDouble(split[7])); // Speed over ground
                    bearing = Util.parseDouble(split[8]); // Course over ground
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
                    } else if(Math.abs(System.currentTimeMillis() - lastFixMillis) > 60000) {
                        Log.w(NMEA_TAG, String.format("System clock off by %ds", System.currentTimeMillis() - lastFixMillis));
                    }

                    // Computed parameters
                    vN = groundSpeed * Math.cos(Math.toRadians(bearing));
                    vE = groundSpeed * Math.sin(Math.toRadians(bearing));

                    // Log.i(NMEA_TAG, "["+time+"] " + Convert.latlong(latitude, longitude) + ", groundSpeed = " + Convert.speed(groundSpeed) + ", bearing = " + Convert.bearing2(bearing));

                    if(Util.isReal(latitude) || Util.isReal(longitude) || Util.isReal(vN) || Util.isReal(vE)) {
                        // Update the official location!
                        MyLocationManager.updateLocation();
                    }
                    break;
                case "GNS":
                    // Fixes data for single or combined (GPS, GLONASS, etc) satellite navigation systems
                    lastFixMillis = dateTime + NMEA.parseTime(split[1]);
                    latitude = NMEA.parseDegreesMinutes(split[2], split[3]);
                    longitude = NMEA.parseDegreesMinutes(split[4], split[5]);
                    if (!split[9].equals("")) {
                        altitude_gps = Util.parseDouble(split[9]);
                        // double geoidSeparation = parseDouble(split[10]]);
                    }
                    if (!split[7].equals("")) {
                        satellitesUsed = Integer.parseInt(split[7]);
                    }
                    break;
//                case "GLL":
//                    // Lat/Long data
//                    latitude = parseDegreesMinutes(split[1], split[2]);
//                    longitude = parseDegreesMinutes(split[3], split[4]);
//                    long time = parseUTC(split[5]); // 123456 = 12:34:56 UTC
//                    boolean status = split[6].equals("A"); // A = active, V = void
//                    break;
                case "VTG":
                    bearing = Util.parseDouble(split[1]); // Course over ground
                    groundSpeed = Convert.kts2mps(Util.parseDouble(split[5])); // Speed over ground
                    break;
                default:
                    Log.e(NMEA_TAG, "[" + timestamp + "] Unknown NMEA command: " + nmea);
            }
        }
    };

    private static int parseInt(String str) {
        return str.equals("") ? -1 : Integer.parseInt(str);
    }

    /** Null listener does nothing. all data comes from NMEA */
    private static final LocationListener androidLocationListener = new LocationListener() {
        public void onLocationChanged(Location loc) {
            // Log.v("GPS", "onLocationChanged(" + loc + ")");
            if (Util.isReal(loc.getLatitude()) && Util.isReal(loc.getLongitude())) {
                // Always update accuracy
                if (loc.hasAccuracy())
                    hAcc = loc.getAccuracy();
                else
                    hAcc = Float.NaN;

                if (!nmeaReceived) {
                    // Phone is not reporting NMEA data, use location data instead
                    Log.v("GPS", "No NMEA data, falling back to LocationManager: " + loc);
                    lastFixMillis = loc.getTime();
                    latitude = loc.getLatitude();
                    longitude = loc.getLongitude();
                    if (loc.hasAltitude())
                        altitude_gps = loc.getAltitude();
                    if (loc.hasSpeed())
                        groundSpeed = loc.getSpeed();
                    if (loc.hasBearing())
                        bearing = loc.getBearing();

                    if(Util.isReal(groundSpeed) && Util.isReal(bearing)) {
                        vN = groundSpeed * Math.cos(Math.toRadians(bearing));
                        vE = groundSpeed * Math.sin(Math.toRadians(bearing));
                    }

                    // Update official location
                    MyLocationManager.updateLocation();
                }
            }
        }

        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    private static GpsStatus gpsStatus;
    private static GpsStatus.Listener statusListener = new GpsStatus.Listener() {
        @Override
        public void onGpsStatusChanged(int event) {
            switch (event) {
                case GpsStatus.GPS_EVENT_STARTED:
                    break;
                case GpsStatus.GPS_EVENT_STOPPED:
                    break;
                case GpsStatus.GPS_EVENT_FIRST_FIX:
                    break;
                case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                    if(manager != null) {
                        gpsStatus = manager.getGpsStatus(gpsStatus);
                        final Iterable<GpsSatellite> satellites = gpsStatus.getSatellites();
                        int count = 0;
                        int used = 0;
                        for (GpsSatellite sat : satellites) {
                            count++;
                            if (sat.usedInFix()) {
                                used++;
                            }
                        }
                        // if(satellitesInView != count || satellitesUsed != used) {
                        //     Log.v(TAG, "Satellite Status: " + satellitesUsed + "/" + satellitesInView);
                        // }
                        satellitesInView = count;
                        satellitesUsed = used;
                    }
            }
        }
    };

    public static void stop() {
        manager.removeGpsStatusListener(statusListener);
        manager.removeNmeaListener(nmeaListener);
        try {
            manager.removeUpdates(androidLocationListener);
        } catch(SecurityException e) {}
        manager = null;
        _instance = null;

        if(listeners.size() > 0) {
            Log.e(TAG, "Stopping location service, but listeners are still listening");
        }
    }
}
