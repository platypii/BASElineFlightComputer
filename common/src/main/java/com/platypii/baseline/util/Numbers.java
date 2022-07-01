package com.platypii.baseline.util;

import androidx.annotation.Nullable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class Numbers {

    public static boolean isReal(double value) {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    /**
     * Fast integer power x^y
     */
    public static int pow(int x, int y) {
        // base cases
        if (x == 1 || y == 0) return 1;
        else if (y == 1) return x;
        else if (y == 2) return x * x;
        else if (y == 3) return x * x * x;
        // divide and conquer
        final int sqrt = pow(x, y / 2);
        if (y % 2 == 0) return sqrt * sqrt;
        else return x * sqrt * sqrt;
    }

    /**
     * Parse a string into a double, but use NaN instead of exceptions
     */
    public static double parseDouble(@Nullable String str) {
        if (str == null || str.isEmpty()) {
            return Double.NaN;
        } else {
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return Double.NaN;
            }
        }
    }

    /**
     * Parse a string into a double, but use null instead of exceptions or non-real
     */
    @Nullable
    public static Double parseDoubleNull(@Nullable String str) {
        if (str == null || str.isEmpty()) {
            return null;
        } else {
            try {
                final double value = Double.parseDouble(str);
                if (Double.isNaN(value) || Double.isInfinite(value)) {
                    return null;
                } else {
                    return value;
                }
            } catch (NumberFormatException e) {
                return null;
            }
        }
    }

    public static float parseFloat(@Nullable String str) {
        if (str == null || str.isEmpty()) {
            return Float.NaN;
        } else {
            try {
                return Float.parseFloat(str);
            } catch (NumberFormatException e) {
                return Float.NaN;
            }
        }
    }

    public static int parseInt(@Nullable String str, int defaultValue) {
        if (str == null || str.isEmpty()) {
            return defaultValue;
        } else {
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
    }

    private static final DecimalFormatSymbols formatSymbols = DecimalFormatSymbols.getInstance(Locale.US);
    public static final DecimalFormat format2 = new DecimalFormat("#.##", formatSymbols);
    public static final DecimalFormat format3 = new DecimalFormat("#.###", formatSymbols);
    public static final DecimalFormat format6 = new DecimalFormat("#.######", formatSymbols);

}
