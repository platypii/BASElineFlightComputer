package com.platypii.baseline.data;

import com.google.android.gms.maps.model.LatLng;
import android.location.Location;
import android.util.Log;

public class MyLocation extends Measurement {

    private Location loc; // android.location.Location for convenience

    
    public MyLocation(long timeMillis, double latitude, double longitude, double altitude_gps,
                      double vN, double vE,
                      float hAcc, float pdop, float hdop, float vdop,
                      int numSat, float groundDistance) {

        // Load state data (altimeter, flightMode, orientation, etc)
        this.altitude = MyAltimeter.altitude;
        this.climb = MyAltimeter.climb;
        this.loadState();

        assert !Double.isInfinite(vN);
        assert !Double.isInfinite(vE);

        // Store location data
        this.timeMillis = timeMillis;
        this.sensor = "GPS";
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude_gps = altitude_gps;
        this.vN = vN;
        this.vE = vE;
        this.hAcc = hAcc;
        this.pdop = pdop;
        this.hdop = hdop;
        this.vdop = vdop;
        this.numSat = numSat;
        this.groundDistance = groundDistance;
    }

    /**
     * Returns an android Location
     */
    public Location loc() {
        if(loc == null) {
            loc = new Location("gps");
        }
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);
        loc.setAltitude(altitude);
        if(!Float.isNaN(hAcc)) {
            loc.setAccuracy(hAcc);
        }
        return loc;
    }

    public LatLng latLng() {
        return new LatLng(latitude, longitude);
    }

    @Override
    public String toString() {
        return loc().toString();
    }

    /**
     * Moves the location along a bearing (degrees) by a given distance (meters)
     */
    public Location moveDirection(double bearing, double d) {
        final double R = 6371000;

        if(Double.isNaN(latitude) || Double.isNaN(longitude)) {
            Log.e("MyLocation", "lat/long not a number: " + latitude + ", " + longitude);
        }
        if(Math.abs(latitude) < 0.1 && Math.abs(longitude) < 0.1) {
            Log.e("MyLocation", "unlikely lat/long: " + latitude + ", " + longitude);
        }

        double lat = radians(latitude);
        double lon = radians(longitude);
        double bear = radians(bearing);

        double latitude2 = Math.asin( Math.sin(lat)*Math.cos(d/R) + Math.cos(lat)*Math.sin(d/R)*Math.cos(bear) );
        double longitude2 = lon + Math.atan2(Math.sin(bear)*Math.sin(d/R)*Math.cos(lat), Math.cos(d/R)-Math.sin(lat)*Math.sin(latitude2));

//        longitude2 = (longitude2+3*Math.PI) % (2*Math.PI) - Math.PI;  // normalise to -180..+180º

        Location destination = new Location("computed");
        destination.setLatitude(degrees(latitude2));
        destination.setLongitude(degrees(longitude2));
        destination.setAltitude(0);
        return destination;
    }

    // Convert degrees <-> radians
    private static double radians(double degrees) {
        return degrees * Math.PI / 180.0;
    }
    private static double degrees(double radians) {
        return radians * 180.0 / Math.PI;
    }

}
