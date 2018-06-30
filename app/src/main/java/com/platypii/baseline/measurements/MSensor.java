package com.platypii.baseline.measurements;

/**
 * Copies an android SensorEvent
 */
public abstract class MSensor extends Measurement {

    // Sensors
    float gX = Float.NaN;
    float gY = Float.NaN;
    float gZ = Float.NaN;
    float rotX = Float.NaN;
    float rotY = Float.NaN;
    float rotZ = Float.NaN;
    float acc = Float.NaN;

    public abstract float x();
    public abstract float y();
    public abstract float z();

}
