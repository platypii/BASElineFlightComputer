package com.platypii.baseline.util.tensor;

import androidx.annotation.NonNull;
import java.util.Locale;

/**
 * Represents a fast 1x2 matrix
 *
 * [p1 p2]
 */
public class Tensor1x2 {

    public double p1 = 1;
    public double p2 = 0;

    public void set(double z, double v) {
        p1 = z;
        p2 = v;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "[[%f,%f]]", p1, p2);
    }

}
