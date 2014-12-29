package com.platypii.baseline.data.filter;


/**
 * Implements a null Filter
 * @author platypii
 */
public class FilterNull extends Filter {

    public void update(double z, double dt) {
    	if(dt == 0) {
    		this.x = z;
    		this.v = 0;
    	} else {
    		this.x = z;
    		this.v = (z - x) / dt; // m/s
    	}
    }
    
}
