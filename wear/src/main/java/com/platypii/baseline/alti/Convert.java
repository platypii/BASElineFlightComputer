package com.platypii.baseline.alti;

import java.util.Locale;

/**
 * General conversion utility class
 * Internally we always use metric units (meters, m/s, etc)
 * Technically the metric unit for angles should be radians. We use degrees.
 */
public class Convert {

    public static boolean metric = false;

    // Convert to standard metric (1000 * FT = 304.8 * M)
    static final double FT = 0.3048;

    /**
     * Like distance() but converts altitude to a quickly-readable string.
     * For numbers greater than 1000, use kilo-units.
     * Uses default locale. Do NOT use this function to write data to a file.
     * 12.4 kft, 486 ft, 3.2 km
     */
    static String altitude(double m) {
        if(Double.isNaN(m)) {
            return "";
        } else if(Double.isInfinite(m)) {
            return Double.toString(m);
        } else {
            final String localUnits = metric? "m" : "ft";
            final double localValue = metric? m : m * 3.2808399;
            if(localValue < 999.5) {
                return Math.round(localValue) + " " + localUnits;
            } else {
                return String.format(Locale.getDefault(), "%.1f k%s", localValue * 0.001, localUnits);
            }
        }
    }

    /**
     * Convert meters to local units
     * @param m meters
     * @param precision number of decimal places
     * @param units show the units?
     * @return distance string in local units
     */
    static String distance(double m, int precision, boolean units) {
        if(Double.isNaN(m)) {
            return "";
        } else if(Double.isInfinite(m)) {
            return Double.toString(m);
        } else {
            final String unitString = units? (metric? "m" : "ft") : "";
            final double localValue = metric? m : m * 3.2808399;
            if(precision == 0) {
                // Faster special case for integers
                return Math.round(localValue) + unitString;
            } else {
                return String.format("%."+precision+"f%s", localValue, unitString);
            }
        }
    }
}
