package com.platypii.baseline.alti;

public class Util {

    static boolean isReal(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    static double parseDouble(String str) {
        if(str == null || str.isEmpty()) {
            return Double.NaN;
        } else {
            try {
                return Double.parseDouble(str);
            } catch(NumberFormatException e) {
                return Double.NaN;
            }
        }
    }

}
