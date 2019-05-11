package com.platypii.baseline.measurements;

import androidx.annotation.NonNull;

/**
 * A generic measurement (alti, gps, gyro, etc)
 */
public abstract class Measurement {

    public long millis; // Milliseconds since epoch
    public long nano; // Nanoseconds since boot

    public String sensor;

    // All measurements must be able to write out to CSV
    @NonNull
    public abstract String toRow();

    public static final String header = "millis,nano,sensor,pressure,lat,lon,hMSL,velN,velE,numSV,gX,gY,gZ,rotX,rotY,rotZ,acc";

}
