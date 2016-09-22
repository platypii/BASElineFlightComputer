package com.platypii.baseline.util;

import java.util.Locale;

/**
 * General conversion utility class
 * Internally we always use metric units (meters, m/s, etc)
 * Technically the metric unit for angles should be radians. We use degrees.
 */
public class Convert {

    public static boolean metric = false;

    // Convert to standard metric (1000 * FT = 304.8 * M)
    public static final double FT = 0.3048;
    public static final double MPH = 0.44704;
    public static final double KPH = 0.277778;

    // Float equivalents
    public static final float MPHf = 0.44704f;
    public static final float KPHf = 0.277778f;

    // Special glide ratio strings
    public static final String GLIDE_STATIONARY = "Stationary";
    public static final String GLIDE_LEVEL = "Level";
    public static final String GLIDE_VERTICAL = "Vertical";

    /**
     * Convert knots to meters/second
     */
    public static double kts2mps(double knots) {
        return 0.51444444444444444 * knots;
    }

    /**
     * Like distance() but converts altitude to a quickly-readable string
     * 12.4kft, 486ft, 3.2km
     */
    public static String altitude(double m) {
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
                return String.format("%.1f k%s", localValue / 1000.0, localUnits);
            }
        }
    }

    /**
     * Convert meters to local units
     * @param m meters
     * @return distance string in local units
     */
    public static String distance(double m) {
        return distance(m, 0, true);
    }
    /**
     * Convert meters to local units
     * @param m meters
     * @param precision number of decimal places
     * @param units show the units?
     * @return distance string in local units
     */
    public static String distance(double m, int precision, boolean units) {
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

    /**
     * Convert meters/second to local units
     * @param mps meters per second
     * @return speed string in local units
     */
    public static String speed(double mps) {
        return speed(mps, 1, true);
    }
    /**
     * Convert meters/second to local units
     * @param mps meters per second
     * @param precision number of decimal places
     * @param units show the units?
     * @return speed string in local units
     */
    public static String speed(double mps, int precision, boolean units) {
        if(Double.isNaN(mps)) {
            return "";
        } else if(Double.isInfinite(mps)) {
            return Double.toString(mps);
        } else {
            final String unitString = units? (metric? " km/h" : " mph") : "";
            final double localValue = metric? mps * 3.6 : mps * 2.23693629;
            if(precision == 0) {
                // Faster special case for integers
                return Math.round(localValue) + unitString;
            } else {
                return String.format("%."+precision+"f%s", localValue, unitString);
            }
        }
    }

    /**
     * Convert the bearing to a human readable format
     * @param degrees bearing in degrees
     * @return "40° (NE)"
     */
    public static String bearing(double degrees) {
        if(Double.isNaN(degrees)) {
            return "";
        } else {
            if(degrees < 0) degrees += 360;
            if(337.5 <= degrees || degrees < 22.5)
                return "North";
            else if(22.5 <= degrees && degrees < 67.5)
                return "Northeast";
            else if(67.5 <= degrees && degrees < 112.5)
                return "East";
            else if(112.5 <= degrees && degrees < 157.5)
                return "Southeast";
            else if(157.5 <= degrees && degrees < 202.5)
                return "South";
            else if(202.5 <= degrees && degrees < 247.5)
                return "Southwest";
            else if(247.5 <= degrees && degrees < 292.5)
                return "West";
            else if(292.5 <= degrees && degrees < 337.5)
                return "Northwest";
            else
                return "";
        }
    }

    /**
     * Convert the bearing to a human readable format, with more precision
     * @param degrees bearing in degrees
     * @return "40° (NE)"
     */
    public static String bearing2(double degrees) {
        if(Double.isNaN(degrees)) {
            return "";
        } else {
            if(degrees < 0) degrees += 360;
            final String bearingStr = ((int) degrees) + "°";
            if(337.5 <= degrees || degrees < 22.5)
                return bearingStr + " (N)";
            else if(22.5 <= degrees && degrees < 67.5)
                return bearingStr + " (NE)";
            else if(67.5 <= degrees && degrees < 112.5)
                return bearingStr + " (E)";
            else if(112.5 <= degrees && degrees < 157.5)
                return bearingStr + " (SE)";
            else if(157.5 <= degrees && degrees < 202.5)
                return bearingStr + " (S)";
            else if(202.5 <= degrees && degrees < 247.5)
                return bearingStr + " (SW)";
            else if(247.5 <= degrees && degrees < 292.5)
                return bearingStr + " (W)";
            else if(292.5 <= degrees && degrees < 337.5)
                return bearingStr + " (NW)";
            else
                return bearingStr;
        }
    }

//    public static String glide(double glideRatio) {
//        return glide(glideRatio, 1, true);
//    }
    public static String glide(double glideRatio, int precision, boolean units) {
        if(Double.isNaN(glideRatio)) {
            return "";
        } else if(Double.isInfinite(glideRatio) || Math.abs(glideRatio) > 40) {
            return GLIDE_LEVEL;
        } else {
            final String value;
            if(glideRatio < 0) {
                value = String.format("+%." + precision + "f", -glideRatio);
            } else {
                value = String.format("%." + precision + "f", glideRatio);
            }
            if(units) {
                return value + " : 1";
            } else {
                return value;
            }
        }
    }

    public static String pressure(double hPa) {
        if(Double.isNaN(hPa))
            return "";
        else
            return String.format(Locale.US, "%.2fhPa", hPa);
    }

//    /**
//     * Convert milliseconds to m:ss or h:mm:ss
//     * @param ms milliseconds
//     * @return "m:ss"
//     */
//    public static String time1(long ms) {
//        final long hour = ms / 3600000;
//        final long min = (ms / 60000) % 60;
//        final long sec = (ms / 1000) % 60;
//        String str;
//        // m:ss
//        if(sec < 10) str = min + ":0" + sec;
//        else str = min + ":" + sec;
//        // h:mm:ss
//        if(hour > 0) {
//            if(min < 10) str = hour + ":0" + str;
//            else str = hour + ":" + str;
//        }
//        return str;
//    }

    /**
     * Convert degrees to local units
     * @param degrees angle in degrees
     * @return angle in local units
     */
    public static String angle(double degrees) {
        // Faster special case for integers
        return Math.round(degrees) + "°";
    }

}
