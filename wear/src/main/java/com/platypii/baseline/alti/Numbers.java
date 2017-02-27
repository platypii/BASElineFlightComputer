package com.platypii.baseline.alti;

public class Numbers {

    static boolean isReal(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

}
