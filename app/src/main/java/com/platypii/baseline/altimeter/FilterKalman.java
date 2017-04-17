package com.platypii.baseline.altimeter;

import android.util.Log;

/**
 * Implements a Kalman Filter
 */
public class FilterKalman extends Filter {
    private static final String TAG = "Kalman";

    // TODO: Acceleration
    // TODO: Determine sensor variance from model error

    // Kalman filter to update altitude and climb
    private static final double sensorVar = 600; // measurement variance
    private static final double accelVar = 8; // acceleration variance

    private static final double r = sensorVar;
    private double p11 = 1;
    private double p12 = 0;
    private double p21 = 0;
    private double p22 = 1;

    public void update(double z, double dt) {
        if(Double.isNaN(x) && !Double.isNaN(z) && dt == 0) {
            // First data point
            this.x = z;
            this.v = 0;
        } else if(dt > 0) {

            // Estimated state
            final double predicted_altitude = x + v * dt;

            // Estimated covariance
            final double q11 = 0.25 * dt*dt*dt*dt * accelVar;
            final double q12 = 0.5 * dt*dt*dt * accelVar;
            final double q21 = 0.5 * dt*dt*dt * accelVar;
            final double q22 = dt*dt * accelVar;
            p11 = (p11 + p12*dt + p21*dt + p22*dt*dt) + q11;
            p12 = (p12 + p22*dt) + q12;
            p21 = (p21 + p22*dt) + q21;
            p22 = (p22) + q22;

            // Kalman gain
            final double k1 = p11 / (p11 + r);
            final double k2 = p21 / (p11 + r);

            // Update state
            final double residual = z - predicted_altitude;
            this.x = predicted_altitude + k1 * residual;
            this.v = v + k2 * residual;

            // Update covariance
            p11 = p11 * (1. - k1);
            p12 = p12 * (1. - k1);
            p21 = -p11 * k2 + p21;
            p22 = -p12 * k2 + p22;

        } else {
            Log.e(TAG, "Unexpected sample data: z = " + z + ", dt = " + dt);
        }

        // Log.i(TAG, "X = " + x + ", v = " + v);
    }

}
