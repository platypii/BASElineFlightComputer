package com.platypii.baseline.data.measurements;

import com.google.android.gms.maps.model.LatLng;
import com.platypii.baseline.util.Util;
import com.platypii.baseline.data.MyAltimeter;

import android.util.Log;

public class MLocation extends Measurement {
    private static final String TAG = "MLocation";

    // GPS
    public final double latitude; // Latitude
    public final double longitude; // Longitude
    public final double altitude_gps; // GPS altitude MSL
    public final double vN; // Velocity north
    public final double vE; // Velocity east
    public float hAcc = Float.NaN; // Horizontal accuracy
    //public float vAcc = Float.NaN; // Vertical accuracy
    //public float sAcc = Float.NaN; // Speed accuracy
    public final float pdop; // Positional dilution of precision
    public final float hdop; // Horizontal dilution of precision
    public final float vdop; // Vertical dilution of precision
    public final int numSat; // Number of satellites
    public final float groundDistance; // Ground distance since app started

    public final double altitude;  // Altitude (m)
    public final double climb;  // Rate of climb (m/s)

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
    public LatLng moveDirection(double bearing, double distance) {
        final double R = 6371000;
        final double d = distance / R;

        if(!Util.isReal(latitude) || !Util.isReal(longitude)) {
            Log.e("MyLocation", "lat/long not a number: " + latitude + ", " + longitude);
        }
        if(Math.abs(latitude) < 0.1 && Math.abs(longitude) < 0.1) {
            Log.e("MyLocation", "unlikely lat/long: " + latitude + ", " + longitude);
        }

        double lat = radians(latitude);
        double lon = radians(longitude);
        double bear = radians(bearing);

        // Precompute trig
        final double sin_d = Math.sin(d);
        final double cos_d = Math.cos(d);
        final double sin_lat = Math.sin(lat);
        final double cos_lat = Math.cos(lat);
        final double sin_d_cos_lat = sin_d * cos_lat;

        final double lat2 = Math.asin(sin_lat * cos_d + sin_d_cos_lat * Math.cos(bear));
        final double lon2 = lon + Math.atan2(Math.sin(bear) * sin_d_cos_lat, cos_d - sin_lat * Math.sin(lat2));

        final double lat3 = degrees(lat2);
        final double lon3 = mod360(degrees(lon2));

        return new LatLng(lat3, lon3);
    }

    private static double radians(double degrees) {
        return degrees * Math.PI / 180.0;
    }
    private static double degrees(double radians) {
        return radians * 180.0 / Math.PI;
    }
    private static double mod360(double degrees) {
        return ((degrees + 540) % 360) - 180;
    }

}
