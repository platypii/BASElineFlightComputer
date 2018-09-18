package com.platypii.baseline.altimeter;

/**
 * Abstract representation of a filter (eg- Low pass, high pass, moving average, kalman, etc)
 * Models a 1 dimensional physical process with position and velocity
 */
public abstract class Filter {

    // Official altitude data
    public abstract double x(); // Meters AGL
    public abstract double v(); // Rate of climb m/s
    // public abstract double a(); // TODO: Vertical acceleration

    /**
     * Initial values for the filter
     * @param z the initial measurement
     * @param v the initial velocity
     */
    public abstract void init(double z, double v);

    /**
     * Process a new measurement
     * @param z the measurement
     * @param dt the change in time since the last measurement. 0 for first reading.
     */
    public abstract void update(double z, double dt);

}
