package com.platypii.baseline.util;

import androidx.annotation.NonNull;
import java.util.Locale;

/**
 * General conversion utility class
 * Internally we always use metric units (meters, m/s, etc)
 * Technically the metric unit for angles should be radians. We use degrees.
 */
public class Convert {

    public static boolean metric = metricDefault();

    // Convert to standard metric (1000 * FT = 304.8 * M)
    public static final double FT = 0.3048;
    public static final double MPH = 0.44704;
    public static final double KPH = 0.277778;
    private static final double MILE = 1609.34;

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
     * Like distance() but converts altitude to a quickly-readable string.
     * For numbers greater than 1000, use kilo-units.
     * Uses default locale. Do NOT use this function to write data to a file.
     * 12.4 kft, 486 ft, 3.2 km
     */
    @NonNull
    public static String altitude(double m) {
        if (Double.isNaN(m)) {
            return "";
        } else if (Double.isInfinite(m)) {
            return Double.toString(m);
        } else {
            final String localUnits = metric ? "m" : "ft";
            final double localValue = metric ? m : m * 3.2808399;
            if (localValue < 999.5) {
                return Math.round(localValue) + " " + localUnits;
            } else {
                return String.format(Locale.getDefault(), "%.1f k%s", localValue * 0.001, localUnits);
            }
        }
    }

    /**
     * Convert meters to local units
     *
     * @param m meters
     * @return distance string in local units
     */
    @NonNull
    public static String distance(double m) {
        return distance(m, 0, true);
    }

    /**
     * Convert meters to local units
     *
     * @param m meters
     * @param precision number of decimal places
     * @param units show the units?
     * @return distance string in local units
     */
    @NonNull
    public static String distance(double m, int precision, boolean units) {
        if (Double.isNaN(m)) {
            return "";
        } else if (Double.isInfinite(m)) {
            return Double.toString(m);
        } else {
            final String unitString = units ? (metric ? " m" : " ft") : "";
            final double localValue = metric ? m : m * 3.2808399;
            if (precision == 0) {
                // Faster special case for integers
                return Math.round(localValue) + unitString;
            } else {
                return String.format("%." + precision + "f%s", localValue, unitString);
            }
        }
    }

    /**
     * Shortened distance intended to be spoken, with units
     *
     * @param m meters
     * @param precision number of decimal places
     */
    @NonNull
    public static String distance2(double m, int precision) {
        if (Double.isNaN(m)) {
            return "";
        } else if (Double.isInfinite(m)) {
            return Double.toString(m);
        } else {
            if (metric) {
                if (m >= 1000) {
                    final String localValue = ConvertUtil.formatDouble(m * 0.001, precision);
                    // Special case for singular units
                    if (localValue.equals("1")) {
                        return "1 kilometer";
                    } else {
                        return localValue + " kilometers";
                    }
                } else {
                    return ConvertUtil.formatInt(m, precision) + " meters";
                }
            } else {
                if (m >= MILE) {
                    // Need max because of float error
                    final double miles = Math.max(1, m * 0.000621371192);
                    final String localValue = ConvertUtil.formatDouble(miles, precision);
                    // Special case for singular units
                    if (localValue.equals("1")) {
                        return "1 mile";
                    } else {
                        return localValue + " miles";
                    }
                } else {
                    return ConvertUtil.formatInt(m * 3.2808399, precision) + " feet";
                }
            }
        }
    }

    /**
     * Shortened distance intended to be displayed, with units
     *
     * @param m meters
     */
    @NonNull
    public static String distance3(double m) {
        if (Double.isNaN(m)) {
            return "";
        } else if (Double.isInfinite(m)) {
            return Double.toString(m);
        } else {
            if (metric) {
                if (m >= 1000) {
                    return Math.round(m * 0.001) + " km";
                } else {
                    return Math.round(m) + " m";
                }
            } else {
                if (m >= MILE) {
                    // Need max because of float error
                    final double miles = Math.max(1, m * 0.000621371192);
                    return Math.round(miles) + " mi";
                } else {
                    return Math.round(m * 3.2808399) + " ft";
                }
            }
        }
    }

    /**
     * Convert meters/second to local units
     *
     * @param mps meters per second
     * @return speed string in local units
     */
    @NonNull
    public static String speed(double mps) {
        final double smallMps = metric ? 10 * KPH : 10 * MPH;
        if (mps < smallMps) {
            return speed(mps, 1, true);
        } else {
            return speed(mps, 0, true);
        }
    }

    /**
     * Convert meters/second to local units
     *
     * @param mps meters per second
     * @param precision number of decimal places
     * @param units show the units?
     * @return speed string in local units
     */
    @NonNull
    public static String speed(double mps, int precision, boolean units) {
        if (Double.isNaN(mps)) {
            return "";
        } else if (Double.isInfinite(mps)) {
            return Double.toString(mps);
        } else {
            final String unitString = units ? (metric ? " km/h" : " mph") : "";
            final double localValue = metric ? mps * 3.6 : mps * 2.23693629;
            if (precision == 0) {
                // Faster special case for integers
                return Math.round(localValue) + unitString;
            } else {
                return String.format("%." + precision + "f%s", localValue, unitString);
            }
        }
    }

    @NonNull
    public static String glide(double glideRatio, int precision, boolean units) {
        if (Double.isNaN(glideRatio)) {
            return "";
        } else if (Double.isInfinite(glideRatio) || Math.abs(glideRatio) > 40) {
            return GLIDE_LEVEL;
        } else {
            final String value;
            if (glideRatio < 0) {
                value = String.format("+%." + precision + "f", -glideRatio);
            } else {
                value = String.format("%." + precision + "f", glideRatio);
            }
            if (units) {
                return value + " : 1";
            } else {
                return value;
            }
        }
    }

    @NonNull
    public static String glide(double groundSpeed, double climb, int precision, boolean units) {
        final double glideRatio = -groundSpeed / climb;
        if (Double.isNaN(glideRatio)) {
            return "";
        } else if (groundSpeed + Math.abs(climb) < 0.5) { // ~1 mph
            return Convert.GLIDE_STATIONARY;
        } else if (Double.isInfinite(glideRatio) || Math.abs(glideRatio) > 30) {
            return Convert.GLIDE_LEVEL;
        } else if (groundSpeed < 0.5 && Math.abs(climb) > 0.5) {
            return Convert.GLIDE_VERTICAL;
        } else {
            final String value;
            if (glideRatio < 0) {
                value = String.format("+%." + precision + "f", -glideRatio);
            } else {
                value = String.format("%." + precision + "f", glideRatio);
            }
            if (units) {
                return value + " : 1";
            } else {
                return value;
            }
        }
    }

    /**
     * Convert.glide2 is used by SpeedChart, and uses empty string more than Convert.glide
     */
    @NonNull
    public static String glide2(double groundSpeed, double climb, int precision, boolean units) {
        final double glideRatio = -groundSpeed / climb;
        if (Double.isNaN(glideRatio)) {
            return "";
        } else if (groundSpeed + Math.abs(climb) < 0.5) { // ~1 mph
            return ""; // Stationary
        } else if (Double.isInfinite(glideRatio) || Math.abs(glideRatio) > 30) {
            return ""; // Level
        } else if (groundSpeed < 0.5 && Math.abs(climb) > 0.5) {
            return ""; // Vertical
        } else {
            final String value;
            if (glideRatio < 0) {
                value = String.format("+%." + precision + "f", -glideRatio);
            } else {
                value = String.format("%." + precision + "f", glideRatio);
            }
            if (units) {
                return value + " : 1";
            } else {
                return value;
            }
        }
    }

    /**
     * Convert pressure to nice hPa string in default locale
     */
    @NonNull
    public static String pressure(double hPa) {
        if (Double.isNaN(hPa))
            return "";
        else
            return String.format(Locale.getDefault(), "%.2f hPa", hPa);
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
//        if (sec < 10) str = min + ":0" + sec;
//        else str = min + ":" + sec;
//        // h:mm:ss
//        if (hour > 0) {
//            if (min < 10) str = hour + ":0" + str;
//            else str = hour + ":" + str;
//        }
//        return str;
//    }

    /**
     * Convert degrees to int degrees
     *
     * @param degrees angle in degrees
     * @return angle in degrees
     */
    @NonNull
    public static String angle(double degrees) {
        // Check for non numbers
        if (Double.isNaN(degrees) || Double.isInfinite(degrees)) return "";
        // Faster special case for integers
        final int degreesInt = (int) Math.floor(degrees);
        return degreesInt + "°";
    }

    /**
     * Convert yaw angle to a human readable format
     *
     * @param degrees yaw angle in degrees
     * @return "15 right" (degrees)
     */
    @NonNull
    public static String angle2(double degrees) {
        // Adjust range to -180..180
        degrees = (degrees + 540) % 360 - 180;
        if (degrees < 0)
            return ConvertUtil.formatInt(-degrees, 2) + " left";
        else if (degrees == 0)
            return "straight";
        else if (degrees > 0)
            return ConvertUtil.formatInt(degrees, 2) + " right";
        else
            return "";
    }

//    /**
//     * Convert the bearing to a human readable format
//     * @param degrees bearing in degrees
//     * @return "40° (NE)"
//     */
//    public static String bearing(double degrees) {
//        if (Double.isNaN(degrees)) {
//            return "";
//        } else {
//            if (degrees < 0) degrees += 360;
//            if (337.5 <= degrees || degrees < 22.5)
//                return "North";
//            else if (22.5 <= degrees && degrees < 67.5)
//                return "Northeast";
//            else if (67.5 <= degrees && degrees < 112.5)
//                return "East";
//            else if (112.5 <= degrees && degrees < 157.5)
//                return "Southeast";
//            else if (157.5 <= degrees && degrees < 202.5)
//                return "South";
//            else if (202.5 <= degrees && degrees < 247.5)
//                return "Southwest";
//            else if (247.5 <= degrees && degrees < 292.5)
//                return "West";
//            else if (292.5 <= degrees && degrees < 337.5)
//                return "Northwest";
//            else
//                return "";
//        }
//    }

    /**
     * Convert the bearing to a human readable format, with more precision
     *
     * @param degrees bearing in degrees
     * @return "40° (NE)"
     */
    @NonNull
    public static String bearing2(double degrees) {
        if (Double.isNaN(degrees)) {
            return "";
        } else {
            degrees %= 360;
            if (degrees < 0) degrees += 360;
            final String bearingStr = ((int) degrees) + "°";
            if (337.5 <= degrees || degrees < 22.5)
                return bearingStr + " (N)";
            else if (degrees < 67.5)
                return bearingStr + " (NE)";
            else if (degrees < 112.5)
                return bearingStr + " (E)";
            else if (degrees < 157.5)
                return bearingStr + " (SE)";
            else if (degrees < 202.5)
                return bearingStr + " (S)";
            else if (degrees < 247.5)
                return bearingStr + " (SW)";
            else if (degrees < 292.5)
                return bearingStr + " (W)";
            else
                return bearingStr + " (NW)";
        }
    }

    /**
     * Returns true if the system default locale indicates metric
     */
    private static boolean metricDefault() {
        final String country = Locale.getDefault().getCountry();
        // Everyone except 'merica
        return !"US".equals(country);
    }

}
