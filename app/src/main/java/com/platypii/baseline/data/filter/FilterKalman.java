package com.platypii.baseline.data.filter;


/**
 * Implements a Kalman Filter
 * @author platypii
 */
public class FilterKalman extends Filter {

	// TODO: Acceleration

    // Kalman filter to update altitude and climb
    private final double sensorVar = 2400; // measurement variance (found experimentally)
    private final double accelVar = 8; // acceleration variance (pulled from my ass)
    
    private final double r = sensorVar;
    private double p11 = 1;
    private double p12 = 0;
    private double p21 = 0;
    private double p22 = 1;
    
    public void update(double z, double dt) {
    	
    	if(dt == 0) {
    		this.x = z;
    		this.v = 0;
    	} else {
    	
	        // Estimated state
	        double predicted_altitude = x + v * dt;
	        
	        // Estimated covariance
	        double q11 = 0.25 * dt*dt*dt*dt * accelVar;
	        double q12 = 0.5 * dt*dt*dt * accelVar;
	        double q21 = 0.5 * dt*dt*dt * accelVar;
	        double q22 = dt*dt * accelVar;
	        p11 = (p11 + p12*dt + p21*dt + p22*dt*dt) + q11;
	        p12 = (p12 + p22*dt) + q12;
	        p21 = (p21 + p22*dt) + q21;
	        p22 = (p22) + q22;
	        
	        // Kalman gain
	        double k1 = p11 / (p11 + r);
	        double k2 = p21 / (p11 + r);
	        
	        // Update state
	        double residual = z - predicted_altitude;
	        this.x = predicted_altitude + k1 * residual;
	        this.v = v + k2 * residual;
	        
	        // Update covariance
	        p11 = p11 * (1. - k1);
	        p12 = p12 * (1. - k1);
	        p21 = -p11 * k2 + p21;
	        p22 = -p12 * k2 + p22;

    	}

        // Log.i("Kalman", "X = " + x + ", v = " + v);
    }
    
}
