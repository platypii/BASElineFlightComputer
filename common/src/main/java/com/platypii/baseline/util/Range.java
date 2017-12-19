package com.platypii.baseline.util;

/**
 * Range represents a min and max
 */
public class Range {

    public double min = Double.NaN;
    public double max = Double.NaN;

    /**
     * Expands the range to include new point
     */
    void expand(double value) {
        if(value < min || Double.isNaN(min)) min = value;
        if(value > max || Double.isNaN(max)) max = value;
    }

}
