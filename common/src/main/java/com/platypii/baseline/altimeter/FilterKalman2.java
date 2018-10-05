package com.platypii.baseline.altimeter;

import com.platypii.baseline.util.tensor.Tensor2x1;
import com.platypii.baseline.util.tensor.Tensor2x2;
import android.util.Log;

/**
 * Implements a Kalman Filter
 *
 * X_k = current estimate
 * K_k = kalman gain
 * Z_k = measured value
 * X_k = K_k Z_k + (1 - K_k) X_{k-1}
 */
public class FilterKalman2 extends Filter {
    private static final String TAG = "Kalman2";

    // TODO: Acceleration
    // TODO: Determine sensor variance from model error

    private final double sensorVariance; // measurement variance ("r" in typical kalman notation)
    private final double accelerationVariance; // acceleration variance

    private final Tensor2x1 x = new Tensor2x1(); // State estimate
    private final Tensor2x1 k = new Tensor2x1(); // Kalman gain
    private final Tensor2x2 p = new Tensor2x2(); // Error covariance
    private final Tensor2x2 q = new Tensor2x2(); // Process noise covariance
    private final Tensor2x2 a = new Tensor2x2(); // dt adjustment

    private boolean initialized = false;

    public FilterKalman2() {
        this(600, 8); // Defaults
    }
    private FilterKalman2(double sensorVariance, double accelerationVariance) {
        this.sensorVariance = sensorVariance;
        this.accelerationVariance = accelerationVariance;
    }

    public void init(double z, double v) {
        x.set(z, v);
        // TODO: Reset params?
        initialized = true;
    }

    @Override
    public void update(double z, double dt) {
        if (!initialized) {
            x.set(z, 0);
            initialized = true;
            return;
        }

        // Check for exceptions
        if (Double.isNaN(z)) {
            Log.e(TAG, "Invalid update: z = NaN");
            return;
        }
        if (dt <= 0) {
            Log.e(TAG, "Invalid update: dt = " + dt);
            return;
        }
        if (!x.isReal()) {
            Log.w(TAG, "Invalid kalman state: x = " + x);
        }

        // Estimated state
        final double predicted_altitude = x.p1 + x.p2 * dt;

        // dt^2, dt^3, dt^4
        final double dt2 = dt * dt;
        final double dt3 = dt2 * dt;
        final double dt4 = dt2 * dt2;

        // Estimated covariance
        q.set(0.25 * dt4, 0.5 * dt3, 0.5 * dt3, dt2);
        q.scale(accelerationVariance);
        // A = [1 dt]
        //     [0  1]
        a.p12 = dt;

        // Estimated error covariance
        // P = A * P * A^T + Q
        a.dot(p, p);
        p.dotTranspose(a, p);
        p.plus(q, p);

        // Kalman gain
        k.set(
                p.p11 / (p.p11 + sensorVariance),
                p.p21 / (p.p11 + sensorVariance)
        );

        // Update state
        final double residual = z - predicted_altitude;
        x.set(
                predicted_altitude + k.p1 * residual,
                x.p2 + k.p2 * residual
        );

        // Update error covariance
        p.set(
                p.p11 * (1. - k.p1),
                p.p12 * (1. - k.p1),
                -p.p11 * k.p2 + p.p21,
                -p.p12 * k.p2 + p.p22
        );
    }

    @Override
    public double x() {
        return x.p1;
    }

    @Override
    public double v() {
        return x.p2;
    }

}
