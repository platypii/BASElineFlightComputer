package com.platypii.baseline.data.measurements;

/**
 * A generic measurement (alti, gps, gyro, etc)
 * @author platypii
 */
public abstract class Measurement {
    
    public long timeMillis; // Milliseconds since epoch
    
    public String sensor;
    
    // Altimeter
    //public long timeNano = -1; // Nanoseconds reported by android SensorEvent
    public double altitude = Double.NaN; // Altitude
    public double climb = Double.NaN; // Rate of climb
    public double pressure = Double.NaN; // Barometric pressure (hPa)
    
    // All measurements must be able to write out to CSV
    // timeMillis, sensor, altitude, climb, pressure, latitude, longitude, altitude_gps, vN, vE, satellites, gX, gY, gZ, rotX, rotY, rotZ, acc
    public abstract String toRow();
}

