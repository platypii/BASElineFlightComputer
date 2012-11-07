package com.platypii.baseline.data.filter;


/**
 * Implements an Alpha Beta Filter
 * @author platypii
 */
public class FilterAlphaBeta extends Filter {

    public void update(double z, double dt) {
    	
    	if(dt == 0) {
    		this.x = z;
    		this.v = 0;
    	} else {
    		// Alpha beta filter (moving average for position+velocity)
    		final double alpha = 0.2;
    		final double beta = 0.2; 
    		final double residual = z - (x + v);
    		this.x = (x + v) + alpha * residual;
    		this.v = v + beta * residual / dt;
    	}

        // Log.i("Kalman", "X = " + x + ", v = " + v);
    }
    
}
