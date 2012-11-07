package com.platypii.baseline.data.filter;

import com.platypii.baseline.data.Matrix;

import android.util.Log;


/**
 * Implements a Kalman Filter
 * @author platypii
 */
public class FilterKalman3 extends Filter {

	// TODO: Acceleration

    // Kalman filter to update altitude and climb
    private final double sensorVar = 2400; // measurement variance (found experimentally)
    private final double accelVar = 8; // acceleration variance (pulled from my ass)
    
    private Matrix A = new Matrix(new double[][] {{1,1}, {0,1}});
    private final Matrix C = new Matrix(new double[][] {{1,0}});
    private final Matrix R = new Matrix(new double[][] {{sensorVar}}); // measurement noise
    private Matrix Q = new Matrix(new double[][] {{0.25 * accelVar, 0.5 * accelVar}, {0.5 * accelVar, accelVar}}); // process noise
    private Matrix X = new Matrix(new double[][] {{0}, {0}}); // initial state
    private Matrix P = new Matrix(new double[][] {{1,0}, {0,1}}); // initial covariance
    private Matrix z = new Matrix(new double[][] {{0}}); // measurement

    
    // Different expression of kalman
    public void update(double altitude_raw, double dt) {
        A.data[0][1] = dt;
        Q.data[0][0] = 0.25 * dt*dt*dt*dt * accelVar;
        Q.data[0][1] = 0.5 * dt*dt*dt * accelVar;
        Q.data[1][0] = 0.5 * dt*dt*dt * accelVar;
        Q.data[1][1] = dt*dt * accelVar;
        
        Matrix S = C.dot(P.dot(C.transpose())) .plus (R);
        Matrix K = A.dot(P.dot(C.transpose())) .dot (S.inverse()); // kalman gain
        
        z.data[0][0] = altitude_raw;
        Matrix res = z .minus (C.dot(A.dot(X))); // residual
        P = A.dot(P.dot(A.transpose())) .plus (Q) .minus (K.dot(C.dot(P.dot(A.transpose())))); // noise estimate
        X = A.dot(X) .plus (K.dot(res)); // state estimate
        
        Log.w("Kalman2", "Estimate: x = " + x + ", P = " + P);
        Log.w("Kalman2", "K = " + K + ", z = " + z);

        x = X.data[0][0];
        v = X.data[1][0];
    }
    
}
