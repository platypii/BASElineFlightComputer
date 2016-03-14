package com.platypii.baseline;

/**
 * Created by platypii on 3/13/16.
 */
public class Util {

    public static boolean isReal(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

}
