package com.platypii.baseline.util.tensor;

import com.platypii.baseline.util.Numbers;
import java.util.Locale;

/**
 * Represents a fast 2x1 matrix
 *
 * [p1]
 * [p2]
 */
public class Tensor2x1 {

    public double p1 = 1;
    public double p2 = 0;

    /**
     * Return true iff all numbers are real
     */
    public boolean isReal() {
        return Numbers.isReal(p1) && Numbers.isReal(p2);
    }

    public void set(double z, double v) {
        p1 = z;
        p2 = v;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "[[%f],[%f]]", p1, p2);
    }

}
