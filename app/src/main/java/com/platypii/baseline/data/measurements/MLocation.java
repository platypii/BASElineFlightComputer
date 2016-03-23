package com.platypii.baseline.data.measurements;

import com.google.android.gms.maps.model.LatLng;
import com.platypii.baseline.util.Util;
import com.platypii.baseline.data.MyAltimeter;

import android.util.Log;

public class MLocation extends Measurement {
    private static final String TAG = "MLocation";

    // GPS
    public double latitude = Double.NaN; // Latitude
    public double longitude = Double.NaN; // Longitude
    public double altitude_gps = Double.NaN; // GPS altitude MSL
    public double vN = Double.NaN; // Velocity north
    public double vE = Double.NaN; // Velocity east
    public float hAcc = Float.NaN; // Horizontal accuracy
    //public float vAcc = Float.NaN; // Vertical accuracy
    //public float sAcc = Float.NaN; // Speed accuracy
    public float pdop = Float.NaN; // Positional dilution of precision
    public float hdop = Float.NaN; // Horizontal dilution of precision
    public float vdop = Float.NaN; // Vertical dilution of precision
    public int numSat = -1; // Number of satellites
    public float groundDistance = Float.NaN; // Ground distance since app started

    public MLocation(long millis, double latitude, double longitude, double altitude_gps,
                     double vN, double vE,
                     float hAcc, float pdop, float hdop, float vdop,
                     int numSat, float groundDistance) {

        // Load state data (altimeter, flightMode, orientation, etc)
        this.altitude = MyAltimeter.altitude;
        this.climb = MyAltimeter.climb;

        // Sanity checks
        if(!Util.isReal(latitude) || !Util.isReal(longitude)) {
            Log.e(TAG, "lat/long not a number: " + this);
        }
        if(Math.abs(latitude) < 0.1 && Math.abs(longitude) < 0.1) {
            Log.e(TAG, "unlikely lat/long: " + latitude + ", " + longitude);
        }
        if(Double.isInfinite(vN) || Double.isInfinite(vE)) {
            Log.e(TAG, "infinite velocity: vN = " + vN + ", vE = " + vE);
        }

        // Store location data
        this.millis = millis;
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

    @Override
    public String toRow() {
        final String sat_str = (numSat != -1)? Integer.toString(numSat) : "";
        final String vN_str = Util.isReal(vN)? Double.toString(vN) : "";
        final String vE_str = Util.isReal(vE)? Double.toString(vE) : "";
        // millis,nano,sensor,pressure,lat,lon,hMSL,velN,velE,numSV,gX,gY,gZ,rotX,rotY,rotZ,acc
        return String.format("%d,,gps,,%f,%f,%f,%s,%s,%s", millis, latitude, longitude, altitude_gps, vN_str, vE_str, sat_str);
    }

    @Override
    public String toString() {
        return String.format("MLocation(%.6f,%.6f,%.1f,%.0f,%.0f)", latitude, longitude, altitude_gps, vN, vE);
    }

    public double groundSpeed() {
        return Math.sqrt(vN * vN + vE * vE);
    }

    public double totalSpeed() {
        if(!Double.isNaN(climb)) {
            return Math.sqrt(vN * vN + vE * vE + climb * climb);
        } else {
            // If we don't have altimeter data, fall back to ground speed
            return Math.sqrt(vN * vN + vE * vE);
        }
    }

    public double glideRatio() {
        return - groundSpeed() / climb;
    }

    public double glideAngle() {
        return Math.toDegrees(Math.atan2(MyAltimeter.climb, groundSpeed()));
    }

    public double bearing() {
        return Math.atan2(vN, vE);
    }

    public LatLng latLng() {
        return new LatLng(latitude, longitude);
    }

    /**
     * Moves the location along a bearing (degrees) by a given distance (meters)
     */
    public LatLng moveDirection(double bearing, double d) {
        final double R = 6371000;

        if(!Util.isReal(latitude) || !Util.isReal(longitude)) {
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

        return new LatLng(degrees(latitude2), degrees(longitude2));
    }

    private static double radians(double degrees) {
        return degrees * Math.PI / 180.0;
    }
    private static double degrees(double radians) {
        return radians * 180.0 / Math.PI;
    }

}
