package com.platypii.baseline.util;

import android.support.annotation.Nullable;

public class Numbers {

    public static boolean isReal(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    /**
     * Fast integer power x^y
     */
    public static int pow(int x, int y) {
        // base cases
        if(x == 1 || y == 0) return 1;
        else if(y == 1) return x;
        else if(y == 2) return x * x;
        else if(y == 3) return x * x * x;
        // divide and conquer
        final int sqrt = pow(x, y / 2);
        if(y % 2 == 0) return sqrt * sqrt;
        else return x * sqrt * sqrt;
    }

    public static double parseDouble(@Nullable String str) {
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

    public static float parseFloat(@Nullable String str) {
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

    public static int parseInt(@Nullable String str, int defaultValue) {
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

    public static long parseLong(@Nullable String str, long defaultValue) {
        if(str == null || str.isEmpty()) {
            return defaultValue;
        } else {
            try {
                return Long.parseLong(str);
            } catch(NumberFormatException e) {
                Exceptions.report(e);
                return defaultValue;
            }
        }
    }
}
