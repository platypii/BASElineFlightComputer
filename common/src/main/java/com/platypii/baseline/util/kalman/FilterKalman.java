package com.platypii.baseline.util.kalman;

import com.platypii.baseline.util.tensor.Tensor1x2;
import com.platypii.baseline.util.tensor.Tensor2x1;
import com.platypii.baseline.util.tensor.Tensor2x2;
import androidx.annotation.NonNull;
import android.util.Log;

/**
 * Implements a Kalman Filter
 */
public class FilterKalman implements Filter {
    private static final String TAG = "Kalman";

    // TODO: Acceleration
    // TODO: Determine sensor variance from model error

    private final double sensorVariance; // measurement variance ("r" in typical kalman notation)
    private final double accelerationVariance; // acceleration variance

    private final Tensor2x1 x = new Tensor2x1(); // State estimate
    private final Tensor2x1 k = new Tensor2x1(); // Kalman gain
    private final Tensor2x2 p = new Tensor2x2(); // Error covariance
    private final Tensor2x2 q = new Tensor2x2(); // Process noise covariance
    private final Tensor2x2 a = new Tensor2x2(); // dt adjustment

    private final Tensor1x2 h = new Tensor1x2(); // Identity
    private final Tensor2x2 temp = new Tensor2x2(); // Scratch space

    private static final int INIT0 = 0; // No samples
    private static final int INIT1 = 1; // First sample, x initialized
    private static final int READY = 2; // Second sample, v initialized
    private int filterState = INIT0;

    public FilterKalman() {
        this(600, 8); // Defaults
    }
    private FilterKalman(double sensorVariance, double accelerationVariance) {
        this.sensorVariance = sensorVariance;
        this.accelerationVariance = accelerationVariance;
    }

    @Override
    public void update(double z, double dt) {
        // Check for input exceptions
        if (Double.isNaN(z)) {
            Log.e(TAG, "Invalid update: z = NaN");
            return;
        }

        // Filter initialization
        if (filterState == INIT0) {
            if (dt != 0) {
                Log.e(TAG, "Invalid initial update: dt = " + dt);
            }
            x.set(z, 0);
            filterState = INIT1;
            return;
        } else if (filterState == INIT1) {
            if (dt <= 0) {
                Log.e(TAG, "Invalid second update: dt = " + dt);
                x.set(z, 0);
            } else {
                final double v = (z - x.p1) / dt;
                x.set(z, v);
                filterState = READY;
            }
            return;
        }

        // Ignore invalid time delta
        if (dt <= 0) {
            Log.e(TAG, "Invalid update: dt = " + dt);
            return;
        }
        // Warn on invalid kalman state
        if (!x.isReal()) {
            Log.w(TAG, "Invalid kalman state: x = " + x);
        }

        // dt^2, dt^3, dt^4
        final double dt2 = dt * dt;
        final double dt3 = dt2 * dt;
        final double dt4 = dt2 * dt2;

        // A = [1 dt]
        //     [0  1]
        a.p12 = dt;

        // Estimated state
        // X = A X
        a.dot(x, x);

        // Estimated covariance
        q.set(0.25 * dt4, 0.5 * dt3, 0.5 * dt3, dt2);
        q.scale(accelerationVariance);

        // Estimated error covariance
        // P = A * P * A^T + Q
        a.dot(p, p);
        p.dotTranspose(a, p);
        p.plus(q, p);

        // Kalman gain
        // K = P H^T (H P H^T + R)^-1 = P / (p1 + R)
        k.set(
                p.p11 / (p.p11 + sensorVariance),
                p.p21 / (p.p11 + sensorVariance)
        );

        // Update state
        // X = X + K (z - H X)
        final double residual = z - x.p1;
        x.set(
                x.p1 + k.p1 * residual,
                x.p2 + k.p2 * residual
        );

        // Update error covariance
        // P = (1 - K * H) * P = P - K * H * P
        k.dot(h, temp);
        temp.dot(p, temp);
        temp.scale(-1);
        p.plus(temp, p);
    }

    @Override
    public double x() {
        return x.p1;
    }

    @Override
    public double v() {
        return x.p2;
    }

    @NonNull
    @Override
    public String toString() {
        return x.toString();
    }

}
