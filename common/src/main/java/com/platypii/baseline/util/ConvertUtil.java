package com.platypii.baseline.util;

import java.util.Arrays;

/**
 * General conversion utility class
 * Internally we always use metric units (meters, m/s, etc)
 * Technically the metric unit for angles should be radians. We use degrees.
 */
class ConvertUtil {

    /**
     * Format a double using a given precision (significant digits)
     */
    static String formatDouble(double value, int precision) {
        // Switch negative, so that we floor toward zero
        if (value < 0) return "-" + formatDouble(-value, precision);
        // Check for special values
        if (Double.isNaN(value) || Double.isInfinite(value)) return Double.toString(value);
        if (value == 0.0) return "0";
        // Precision must be at least 1
        if (precision <= 0) precision = 1;
        // Find magnitude of value
        final int mag = (int) Math.floor(Math.log10(value));
        // Significant digits as an int (9300 -> 93, 9.3 -> 93, 0.093 -> 93)
        final int digits = (int) Math.floor(value * Math.pow(10, precision - mag - 1));
        // How many decimal places we need to print
        final int decimalPlaces = precision - mag - 1;
        if (decimalPlaces <= 0) {
            // Add trailing zeros 9300
            final char[] zeros = new char[-decimalPlaces];
            Arrays.fill(zeros, '0');
            return digits + new String(zeros);
        } else if (precision < decimalPlaces) {
            // Add leading zeros .093
            final char[] zeros = new char[decimalPlaces - precision];
            Arrays.fill(zeros, '0');
            return "." + new String(zeros) + digits;
        } else {
            // Split digits 9.3
            final String digitsString = Integer.toString(digits);
            final String before = digitsString.substring(0, precision - decimalPlaces);
            final String after = digitsString.substring(precision - decimalPlaces);
            return before + "." + after;
        }
    }

    /** Truncate to at most 2 int digits */
    static String formatInt(double value, int precision) {
        // Switch negative, so that we floor toward zero
        if (value < 0) return "-" + formatInt(-value, precision);
        // Precision must be at least 1
        if (precision <= 0) precision = 1;
        // Convert to int
        final int valueInt = (int) Math.floor(value);
        final int mag = (int) Math.floor(Math.log10(value));
        if (mag < precision) {
            // No need to truncate
            return Integer.toString(valueInt);
        } else {
            final int mask = (int) Math.pow(10, mag - precision + 1);
            final int truncated = valueInt - (valueInt % mask);
            return Integer.toString(truncated);
        }
    }

}
