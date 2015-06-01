package com.platypii.baseline.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.GpsStatus;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.platypii.baseline.data.measurements.MLocation;


public class MyLocationManager {

    // Singleton GPSManager
    private static MyLocationManager _instance;
    
    // Android Location manager
    private static LocationManager manager;
    
    // Listeners
    private static final List<MyLocationListener> listeners = new ArrayList<>();
    
    // GPS status
    public static boolean isEnabled = true;
    public static float refreshRate = 0; // Moving average of refresh rate in Hz
    
    // Satellite data
    public static int satellitesInView; // Satellites in view
    public static int satellitesUsed; // Satellites used in last fix 

    // Most recent data
    public static long lastFixMillis = -1;
    public static double latitude = Double.NaN;
    public static double longitude = Double.NaN;
    public static double altitude_gps = Double.NaN;
    public static double vN = Double.NaN;
    public static double vE = Double.NaN;
    public static double groundSpeed = Double.NaN;
    public static double speed = Double.NaN;
    public static double bearing = Double.NaN;
    public static double glide = Double.NaN;
    public static double glideAngle = Double.NaN;
    public static float hAcc = Float.NaN;
    //public static float vAcc = Float.NaN;
    //public static float sAcc = Float.NaN;
    public static float pdop = Float.NaN;
    public static float hdop = Float.NaN;
    public static float vdop = Float.NaN;
    private static long dateTime; // The number of milliseconds until the start of this day, midnight GMT

    // Computed parameters
    public static float groundDistance = 0;
    
    // History
    public static MLocation lastLoc; // last location received
    public static MLocation prevLoc; // 2nd to last

    private static final int maxHistory = 600; // Maximum number of measurements to keep in memory
    public static final SyncedList<MLocation> history = new SyncedList<>(maxHistory);


    /**
     * Initializes location services
     * @param context The Application context
     */
    public static synchronized void initLocation(Context context) {
        if(_instance == null) {
            _instance = new MyLocationManager();
            
            // Obtain GPS locations
            manager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
            if(isEnabled) {
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, nullListener);
                manager.addNmeaListener(nmeaListener);
            }
            
            // TODO: Start an interval timer to update when signal is lost
            
        }
    }

    /**
     * Add a new listener to be notified of location updates
     */
    public static void addListener(MyLocationListener listener) {
        synchronized(listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener from location updates
     */
    public static void removeListener(MyLocationListener listener) {
        synchronized(listeners) {
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

        if(prevLoc != null) {
            // Compute distance
            tempLoc1.setLatitude(prevLoc.latitude);
            tempLoc1.setLongitude(prevLoc.longitude);
            tempLoc2.setLatitude(lastLoc.latitude);
            tempLoc2.setLongitude(lastLoc.longitude);
            groundDistance += tempLoc1.distanceTo(tempLoc2);

            // Clear out old values
            lastFixMillis = -1;
            latitude = Double.NaN;
            longitude = Double.NaN;
            altitude_gps = Double.NaN;
            vN = Double.NaN;
            vE = Double.NaN;
            groundSpeed = Double.NaN;
            speed = Double.NaN;
            bearing = Double.NaN;
            glide = Double.NaN;
            glideAngle = Double.NaN;
            hAcc = Float.NaN;
            // vAcc = Float.NaN;
            // sAcc = Float.NaN;
            pdop = Float.NaN;
            hdop = Float.NaN;
            vdop = Float.NaN;
            satellitesInView = -1; // Satellites in view
            satellitesUsed = -1; // Satellites used in last fix

            // GPS sample refresh rate
            // TODO: Include time from last sample until now
            final long deltaTime = lastLoc.timeMillis - prevLoc.timeMillis; // time since last refresh
            if(deltaTime > 0) {
                final float refreshTime = 1000.0f / (float) (deltaTime);
                refreshRate += (refreshTime - refreshRate) * 0.5f; // Moving average
                if (Double.isNaN(refreshRate)) {
                    Log.e("MyLocationManager", "Refresh rate is NaN, deltaTime = " + deltaTime + " refreshTime = " + refreshTime);
                    refreshRate = 0;
                }
            }
        }

        // History
        history.append(lastLoc);

        // Notify listeners (using AsyncTask so the manager never blocks!)
        new AsyncTask<MLocation,Void,Void>() {
            @Override
            protected Void doInBackground(MLocation... params) {
                synchronized(listeners) {
                    for(MyLocationListener listener : listeners) {
                        listener.onLocationChanged(params[0]);
                    }
                }
                return null;
            }
        }.execute(lastLoc);
    }

    // NMEA listener
    private static final GpsStatus.NmeaListener nmeaListener = new GpsStatus.NmeaListener() {
        // timestamp is seconds
        public void onNmeaReceived(long timestamp, String nmea) {
            
            // Log.v("NMEA", "["+timestamp+"] " + nmea);

            String split[] = nmea.split("[,*]");
            if(split[0].charAt(0) != '$') Log.e("NMEA", "Invalid NMEA tag: " + split[0]);
            if(split[0].length() == 6) Log.e("NMEA", "Invalid NMEA tag length: " + split[0]);

            assert nmeaChecksum(nmea);

            // Parse command
            if(split[0].equals("$GPGSA")) {
                // Overall satellite data (DOP and active satellites)
                // boolean autoDim = split[1].equals("A"); // A = Auto 2D/3D, M = Forced 2D/3D
                // gpsFix = split[2].equals("")? 0 : Integer.parseInt(split[2]); // 0 = null, 1 = No fix, 2 = 2D, 3 = 3D
                pdop = split[split.length-4].equals("")? Float.NaN : Float.parseFloat(split[split.length-4]);
                hdop = split[split.length-3].equals("")? Float.NaN : Float.parseFloat(split[split.length-3]);
                vdop = split[split.length-2].equals("")? Float.NaN : Float.parseFloat(split[split.length-2]);

            } else if(split[0].equals("$GPGSV")) {
                // Detailed satellite data (satellites in view)
                satellitesInView = split[3].equals("")? 0 : Integer.parseInt(split[3]);
                
            } else if(split[0].equals("$GPGGA")) {
                // Fix data
                latitude = parseDegreesMinutes(split[2], split[3]);
                longitude = parseDegreesMinutes(split[4], split[5]);
                // gpsFix = split[6].equals("")? -1 : Integer.parseInt(split[6]); // 0 = Invalid, 1 = Valid SPS, 2 = Valid DGPS, 3 = Valid PPS
                satellitesUsed = split[7].equals("")? -1 : Integer.parseInt(split[7]);
                hdop = split[8].equals("")? Float.NaN : Float.parseFloat(split[8]);
                if(split[9].equals("")) {
                    assert split[10].equals("");
                    altitude_gps = Double.NaN;
                } else {
                    assert split[10].equals("M"); // Meters
                    altitude_gps = Double.parseDouble(split[9]);
                }
                // double geoidSeparation = split[11].equals("")? Double.NaN : Double.parseDouble(split[11]]); // Geoid separation according to WGS-84 ellipsoid
                // assert split[12].equals("M")// Separation Units
                // double dgpsAge = split[13].equals("")? Double.NaN : Double.parseDouble(split[13]);
                // int dgpsStationId = split[14].equals("")? Double.NaN : Double.parseDouble(split[14]);

                // TODO: hAcc, vAcc, sAcc
                
                // Time. At this point we actually have (at least) 3 choices of clock. 
                // 1) timestamp parameter, is measured in seconds
                // 2) System.currentTime(), depends on how long execution takes to this point
                // 3) GPS time, most accurate, but must be parsed carefully, since we only get milliseconds since midnight GMT
                // split[1]: Time: 123456 = 12:34:56 UTC
                // if(dateTime == 0 || split[1].equals(""))
                //   lastFixMillis = timestamp * 1000; // Alt: System.currentTimeMillis();
                // else 
                //   lastFixMillis = dateTime + parseTime(split[1]);
                assert 0 < lastFixMillis;
                assert Math.abs(System.currentTimeMillis() - lastFixMillis) < 60000; // at most 60 seconds time skew

                // Update the official location!
                MyLocationManager.updateLocation();

            } else if(split[0].equals("$GPRMC")) {
                // Recommended minimum data for gps
                // boolean status = split[2].equals("A"); // A = active, V = void
                latitude = parseDegreesMinutes(split[3], split[4]);
                longitude = parseDegreesMinutes(split[5], split[6]);
                groundSpeed = split[7].equals("")? Double.NaN : Convert.kts2mps(Double.parseDouble(split[7])); // Speed over ground
                bearing = split[8].equals("")? Double.NaN : Double.parseDouble(split[8]); // Course over ground
                // split[10], split[11]: 003.1,W magnetic Variation
                // split[9]: Date: 230394 = 23 March 1994
                // split[1]: Time: 123456 = 12:34:56 UTC
                dateTime = parseDate(split[9]);
                lastFixMillis = dateTime + parseTime(split[1]);
                // Log.w("Time", "["+timestamp+"] lastFixMillis = " + lastFixMillis + ", currentTime = " + System.currentTimeMillis());

                // Computed parameters
                vN = groundSpeed * Math.cos(Math.toRadians(bearing));
                vE = groundSpeed * Math.sin(Math.toRadians(bearing));
                if(!Double.isNaN(MyAltimeter.climb)) {
                    speed = Math.sqrt(groundSpeed * groundSpeed + MyAltimeter.climb * MyAltimeter.climb);
                    glide = -groundSpeed / MyAltimeter.climb;
                    glideAngle = Math.toDegrees(Math.atan(MyAltimeter.climb / groundSpeed));
                } else {
                    // If we don't have altimeter data, fall back to ground speed
                    speed = groundSpeed;
                }
                
                // Log.i("NMEA", "["+time+"] " + Convert.latlong(latitude, longitude) + ", groundSpeed = " + Convert.speed(groundSpeed) + ", bearing = " + Convert.bearing2(bearing));

/*                
            } else if(split[0].equals("$GPGLL")) {
                // Lat/Long data
                latitude = parseDegreesMinutes(split[1], split[2]);
                longitude = parseDegreesMinutes(split[3], split[4]);
                long time = parseUTC(split[5]); // 123456 = 12:34:56 UTC
                boolean status = split[6].equals("A"); // A = active, V = void
 */
            } else if(split[0].equals("$GLGSV")) {
                // GNSS satellite in view
                satellitesInView = Integer.parseInt(split[3]);

            } else if(split[0].equals("$GNGNS")) {
                // Fixes data for single or combined (GPS, GLONASS, etc) satellite navigation systems
                lastFixMillis = dateTime + parseTime(split[1]);
                latitude = parseDegreesMinutes(split[2], split[3]);
                longitude = parseDegreesMinutes(split[4], split[5]);
                if(split[9].equals("")) {
                    assert split[10].equals("");
                    altitude_gps = Double.NaN;
                } else {
                    assert split[10].equals("M"); // Meters
                    altitude_gps = Double.parseDouble(split[9]);
                }
                if(!split[7].equals("")) {
                    satellitesUsed = Integer.parseInt(split[7]);
                }

            } else if(split[0].equals("$GPVTG")) {
                bearing = split[1].equals("")? Double.NaN : Double.parseDouble(split[1]); // Course over ground
                groundSpeed = split[5].equals("")? Double.NaN : Convert.kts2mps(Double.parseDouble(split[5])); // Speed over ground

            } else if(split[0].equals("$GNGSA")) {
                // pdop, hdop, vdop, ...

            } else {
                Log.e("NMEA", "["+timestamp+"] Unknown NMEA command: " + nmea);
            }
        }
    };

    /**
     * Parse DDDMM.MMMM,N into decimal degrees
     * @param dm The latitude or longitude in "DDDMM.MMMM" format
     * @param nsew The modifier "N", "S", "E", or "W"
     * @return The latitude or longitude in decimal degrees
     */
    private static double parseDegreesMinutes(String dm, String nsew) {
        if(dm.equals("")) {
            return Double.NaN;
        } else {
            int index = dm.indexOf('.') - 2;
            assert 0 <= index;
            double m = Double.parseDouble(dm.substring(index));
            int d = (index == 0)? 0 : Integer.parseInt(dm.substring(0, index));
            double degrees = d + m / 60.0; 
            
            if(nsew.equals("S") || nsew.equals("W"))
                return -degrees;
            else
                return degrees;
        }
    }
    
    public static void setEnabled(boolean enable) {
        if(!isEnabled && enable) {
            // Enable
            isEnabled = true;
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, nullListener);
            manager.addNmeaListener(nmeaListener);
        } else if(isEnabled && !enable) {
            // Disable
            isEnabled = false;
            manager.removeUpdates(nullListener);
            manager.removeNmeaListener(nmeaListener);
        }
    }
    
    private static final Calendar cal = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
    /**
     * Parse DDMMYY into milliseconds since epoch
     */
    private static long parseDate(String date) {
        if(date == null || date.equals("")) {
            return 0;
        } else {
            assert date.length() == 6;
            int day = Integer.parseInt(date.substring(0, 2));
            int month = Integer.parseInt(date.substring(2, 4)) - 1; // january is 0 not 1
            int year = 1900 + Integer.parseInt(date.substring(4, 6));
            if(year < 1970) year += 100;
            cal.set(Calendar.MILLISECOND, 0);
            cal.set(year, month, day, 0, 0, 0);
            return cal.getTime().getTime();
        }
    }
    /**
     * Parse HHMMSS.SS UTC time into milliseconds since midnight
     */
    private static long parseTime(String time) {
        if(time == null || time.equals("")) {
            return 0;
        } else {
            try {
                assert time.indexOf('.') == 6;
                long hour = Integer.parseInt(time.substring(0, 2));
                long min = Integer.parseInt(time.substring(2, 4));
                // double sec = Double.parseDouble(utc.substring(4));
                long sec = Integer.parseInt(time.substring(4, 6));
                long ms = time.length() <= 6? 0 : (long) (1000 * Double.parseDouble(time.substring(6)));
                return hour * 3600000 + min * 60000 + sec * 1000 + ms;
            } catch(Exception e) {
                return 0;
            }
        }
    }
    
    // Returns true if the checksum is valid
    private static boolean nmeaChecksum(String nmea) {
        int starIndex = nmea.indexOf('*');
        
//        android.util.Log.e("NMEA", "nmea = " + nmea);
//        android.util.Log.e("NMEA", "starIndex = " + starIndex + ", length = " + nmea.length());

        assert nmea.charAt(0) == '$';
        assert nmea.charAt(starIndex) == '*';

        short checksum1 = 0;
        for(int i = 1; i < starIndex; i++) {
            checksum1 ^= nmea.charAt(i);
        }
        short checksum2 = Short.parseShort(nmea.substring(starIndex + 1, starIndex + 3), 16);
        return checksum1 == checksum2;
    }
    
    // Null listener does nothing. all data comes from NMEA.
    private static final LocationListener nullListener = new LocationListener() {
        public void onLocationChanged(Location loc) {
            if(loc.hasAccuracy())
                hAcc = loc.getAccuracy();
            else
                hAcc = Float.NaN;
        }
        public void onProviderDisabled(String provider) {}
        public void onProviderEnabled(String provider) {}
        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };
}
