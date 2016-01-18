package com.platypii.baseline.data.measurements;

import com.platypii.baseline.data.MyAltimeter;

import android.util.Log;

public class MLocation extends Measurement {

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

        // Assertions
        if(Double.isInfinite(vN)) Log.e("MLocation", "Infinite vN");
        if(Double.isInfinite(vE)) Log.e("MLocation", "Infinite vE");

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
        final String vN_str = isReal(vN)? Double.toString(vN) : "";
        final String vE_str = isReal(vE)? Double.toString(vE) : "";
        // millis,nano,sensor,pressure,lat,lon,hMSL,velN,velE,numSV,gX,gY,gZ,rotX,rotY,rotZ,acc
        return String.format("%d,,gps,,%f,%f,%f,%s,%s,%s", millis, latitude, longitude, altitude_gps, vN_str, vE_str, sat_str);
    }

    private static boolean isReal(double x) {
        return !(Double.isInfinite(x) || Double.isNaN(x));
    }

}
