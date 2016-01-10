package com.platypii.baseline.data.measurements;

/**
 * A generic measurement (alti, gps, gyro, etc)
 * @author platypii
 */
public abstract class Measurement {

    public long millis; // Milliseconds since epoch
    public long nano; // Nanoseconds since boot

    public String sensor;

    // Altimeter
    //public long timeNano = -1; // Nanoseconds reported by android SensorEvent
    public double altitude = Double.NaN; // Altitude
    public double climb = Double.NaN; // Rate of climb
    public double pressure = Double.NaN; // Barometric pressure (hPa)

    // All measurements must be able to write out to CSV
    public abstract String toRow();

    public static final String header = "millis,nano,sensor,pressure,latitude,longitude,altitude_gps,vN,vE,satellites,gX,gY,gZ,rotX,rotY,rotZ,acc";

}

