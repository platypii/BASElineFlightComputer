package com.platypii.baseline.data;

import java.util.Locale;

/**
 * General conversion utility class
 * Internally we always use metric units (meters, m/s, 
 * Technically the metric unit for angles is radians. We use degrees.
 * @author platypii
 */
public class Convert {

	private static boolean metric = false;

	// Convert to standard metric (1000 * FT = 304.8 * M)
    public static final double FT = 0.3048;

	/**
	 * Convert knots to meters/second
	 */
    public static double kts2mps(double knots) {
        return 0.51444444444444444 * knots;
    }

    /**
     * Convert meters/seconds to mph
     */
    public static double mps2mph(double mps) {
        return 2.23694 * mps;
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
     * @return distance string in local units
     */
	public static String distance(double m, int precision) {
		return distance(m, precision, true);
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
        	String unitString = units? (metric? "m" : "ft") : "";
        	double localValue = metric? m : m * 3.2808399;
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
        	String unitString = units? (metric? "kph" : "mph") : "";
        	double localValue = metric? mps * 3.6 : mps * 2.23693629;
            if(precision == 0) {
                // Faster special case for integers
            	return Math.round(localValue) + unitString;
            } else {
                return String.format("%."+precision+"f%s", localValue, unitString);
            }
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
            String bearingStr = ((int) degrees) + "°";
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
    
    public static String glide(double glide) {
        return glide(glide, 1);
    }
    public static String glide(double glide, int precision) {
        if(Double.isNaN(glide)) {
            return "";
        } else if(Double.isInfinite(glide) || Math.abs(glide) > 40) {
            return "Level";
        } else if(precision == 0) {
            // Faster special case for integers
            return Math.round(glide) + " : 1";
        } else if(glide > 0) {
            return String.format("+%." + precision + "f : 1", glide);
        } else {
            return String.format("%." + precision + "f : 1", glide);
        }
    }
    
    public static String pressure(double hPa) {
        if(Double.isNaN(hPa))
            return "";
        else
            return String.format(Locale.US, "%.2fhPa", hPa);
    }

    /**
     * Convert milliseconds to m:ss or h:mm:ss
     * @param ms milliseconds
     * @return "m:ss"
     */
    public static String time1(long ms) {
        long hour = ms / 3600000;
        long min = (ms / 60000) % 60;
        long sec = (ms / 1000) % 60;
        String str;
        // m:ss
        if(sec < 10) str = min + ":0" + sec;
        else str = min + ":" + sec;
        // h:mm:ss
        if(hour > 0) {
            if(min < 10) str = hour + ":0" + str;
            else str = hour + ":" + str;
        }
        return str;
    }
    
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
