package com.platypii.baseline.util;

public class Numbers {

    public static boolean isReal(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    public static double parseDouble(String str) {
        if(str == null || str.isEmpty()) {
            return Double.NaN;
        } else {
            try {
                return Double.parseDouble(str);
            } catch(NumberFormatException e) {
                Exceptions.report(e);
                return Double.NaN;
            }
        }
    }

    public static float parseFloat(String str) {
        if(str == null || str.isEmpty()) {
            return Float.NaN;
        } else {
            try {
                return Float.parseFloat(str);
            } catch(NumberFormatException e) {
                Exceptions.report(e);
                return Float.NaN;
            }
        }
    }

    public static int parseInt(String str, int defaultValue) {
        if(str == null || str.isEmpty()) {
            return defaultValue;
        } else {
            try {
                return Integer.parseInt(str);
            } catch(NumberFormatException e) {
                Exceptions.report(e);
                return defaultValue;
            }
        }
    }
}
