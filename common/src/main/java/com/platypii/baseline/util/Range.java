package com.platypii.baseline.util;

import java.util.Locale;

/**
 * Range represents a min and max
 */
public class Range {

    public double min = Double.NaN;
    public double max = Double.NaN;

    /**
     * Expands the range to include new point
     */
    public void expand(double value) {
        if (value < min || Double.isNaN(min)) min = value;
        if (value > max || Double.isNaN(max)) max = value;
    }

    public boolean isEmpty() {
        return Double.isNaN(min);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "Range(%f,%f)", min, max);
    }

}
