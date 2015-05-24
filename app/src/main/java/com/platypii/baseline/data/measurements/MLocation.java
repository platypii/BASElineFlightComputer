package com.platypii.baseline.data.measurements;

import com.platypii.baseline.data.MyAltimeter;

import android.location.Location;

public class MLocation extends Measurement {

    private Location loc; // android.location.Location for convenience

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

    public MLocation(long timeMillis, double latitude, double longitude, double altitude_gps,
                     double vN, double vE,
                     float hAcc, float pdop, float hdop, float vdop,
                     int numSat, float groundDistance) {

        // Load state data (altimeter, flightMode, orientation, etc)
        this.altitude = MyAltimeter.altitude;
        this.climb = MyAltimeter.climb;

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

    @Override
    public String toRow() {
        // timeMillis, sensor, altitude, climb, pressure, latitude, longitude, altitude_gps, vN, vE, gX, gY, gZ, rotX, rotY, rotZ, acc
        return String.format("%d,gps,,,,%f,%f,%f,%f,%f", timeMillis, latitude, longitude, altitude_gps, vN, vE);
    }

}
