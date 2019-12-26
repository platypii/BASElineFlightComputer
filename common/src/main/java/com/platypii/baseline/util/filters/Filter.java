package com.platypii.baseline.util.filters;

/**
 * Abstract representation of a filter (eg- Low pass, high pass, moving average, kalman, etc)
 * Models a 1 dimensional physical process with position and velocity
 */
public interface Filter {

    // Official altitude data
    double x(); // Meters AGL

    double v(); // Rate of climb m/s
    // public abstract double a(); // TODO: Vertical acceleration

    /**
     * Process a new measurement
     *
     * @param z the measurement
     * @param dt the change in time since the last measurement. 0 for first reading.
     */
    void update(double z, double dt);

}
