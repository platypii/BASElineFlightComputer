package com.platypii.baseline.data.filter;


/**
 * Implements a Low Pass Filter
 * @author platypii
 */
public class FilterLowPass extends Filter {

    public void update(double z, double dt) {
    	
    	if(dt == 0) {
    		this.x = z;
    		this.v = 0;
    	} else {
    		// Low-pass filter
    		this.x = x + (z - x) * 0.15;
    		this.v = (z - x) / dt; // m/s
    	}

        // Log.i("Kalman", "X = " + x + ", v = " + v);
    }
    
}
