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
    private static final double sensorVariance = 600; // measurement variance ("r" in typical kalman notation)
    private static final double accelerationVariance = 8; // acceleration variance

    private double x = Double.NaN;
    private double v = Double.NaN;
    private double p11 = 1;
    private double p12 = 0;
    private double p21 = 0;
    private double p22 = 1;

    private boolean initialized = false;

    @Override
    public void init(double z, double v) {
        this.x = z;
        this.v = v;
        // TODO: Reset params?
        initialized = true;
    }

    @Override
    public void update(double z, double dt) {
        // Check for exceptions
        if (!initialized) {
            Log.e(TAG, "Invalid update: not initialized");
        }
        if (Double.isNaN(z)) {
            Log.e(TAG, "Invalid update: z = NaN");
            return;
        }
        if (dt <= 0) {
            Log.e(TAG, "Invalid update: dt = " + dt);
            return;
        }
        if (Double.isNaN(x) || Double.isNaN(v)) {
            Log.w(TAG, "Invalid kalman state: x = " + x + " v = " + v);
        }

        // Estimated state
        final double predicted_altitude = x + v * dt;

        // Estimated covariance
        final double q11 = 0.25 * dt*dt*dt*dt * accelerationVariance;
        final double q12 = 0.5 * dt*dt*dt * accelerationVariance;
        final double q21 = 0.5 * dt*dt*dt * accelerationVariance;
        final double q22 = dt*dt * accelerationVariance;
        p11 = (p11 + p12*dt + p21*dt + p22*dt*dt) + q11;
        p12 = (p12 + p22*dt) + q12;
        p21 = (p21 + p22*dt) + q21;
        p22 = (p22) + q22;

        // Kalman gain
        final double k1 = p11 / (p11 + sensorVariance);
        final double k2 = p21 / (p11 + sensorVariance);

        // Update state
        final double residual = z - predicted_altitude;
        this.x = predicted_altitude + k1 * residual;
        this.v = v + k2 * residual;

        // Update covariance
        p11 = p11 * (1. - k1);
        p12 = p12 * (1. - k1);
        p21 = -p11 * k2 + p21;
        p22 = -p12 * k2 + p22;
    }

    @Override
    public double x() {
        return x;
    }

    @Override
    public double v() {
        return v;
    }

}
