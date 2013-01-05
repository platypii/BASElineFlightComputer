package com.platypii.baseline.ui;

import java.util.Locale;
import com.google.android.maps.GeoPoint;
import android.location.Location;
import android.text.format.DateFormat;


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
    public static final double M = 1;
    public static final double MILES = 1609.344;
    public static final double MPH = 0.44704;
    public static final double KPH = 0.277778;
    public static final double FPM = 0.00508;
    public static final double MPS = 1;
    public static final double G = 9.80665;

    public static double localDistance     = FT;
    public static String localDistanceUnits = "ft";
    public static double localSpeed     = MPH;
    public static String localSpeedUnits = "mph";
    public static double localSmallSpeed     = FPM;
    public static String localSmallSpeedUnits = "ft/m";
    public static double localForce     = G;
    public static String localForceUnits = "g";
    public static double localAngle     = 1;
    public static String localAngleUnits = "°";


    /**
	 * Convert miles/hour to meters/second
	 * @param mph miles/hour
	 * @return meters/second
	 */
	public static double mph2mps(double mph) {
		return 0.44704 * mph;
	}

	/**
	 * Convert meters/second to kilometers/hour
	 * @param mps meters/second
	 * @return kilometers/hour
	 */
	public static double mps2kph(double mps) {
		return 3.6 * mps;
	}

	/**
	 * Convert meters/second to miles/hour
	 * @param mps meters/second
	 * @return miles/hour
	 */
	public static double mps2mph(double mps) {
		return 2.23693629 * mps;
	}

	/**
	 * Convert meters to feet
	 */
	public static double m2ft(double meters) {
		return 3.2808399 * meters;
	}

	/**
	 * Convert meters to miles
	 */
	public static double m2mi(double meters) {
		return 0.000621371192 * meters;
	}

	/**
	 * Convert feet to meters
	 */
	public static double ft2m(double feet) {
		return 0.3048 * feet;
	}

	/**
	 * Convert knots to meters/second
	 */
    public static double kts2mps(double knots) {
        return 0.51444444444444444 * knots;
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
     * Convert meters to ft, or miles if applicable
     */
	public static String distance2(double m) {
        if(Double.isNaN(m)) {
            return "";
        } else if(Double.isInfinite(m)) {
            return Double.toString(m);
        } else if(metric) {
        	return Math.round(m) + "m";
        } else {
        	if(m < MILES) {
        		return Math.round(m * 3.2808399) + "ft";
	        } else {
	        	return String.format(Locale.US, "%.2fmi", m * 0.000621371192);
	        }
        }
	}

	/**
	 * Converts the input in local units into internal units
	 */
	public static double unDistance(double local) {
		return metric? local : local * FT;
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
     * @return speed string in local units
     */
	public static String speed(double mps, int precision) {
		return speed(mps, precision, true);
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

	// uses smaller units
    public static String speed2(double mps) {
        return speed2(mps, 0, true);
    }
    public static String speed2(double mps, int precision) {
    	return speed2(mps, 0, true);
    }
    public static String speed2(double mps, int precision, boolean units) {
        if(Double.isNaN(mps)) {
            return "";
        } else if(Double.isInfinite(mps)) {
            return Double.toString(mps);
        } else {
        	String unitString = units? (metric? "m/s" : "ft/m") : "";
        	double localValue = metric? mps : mps * 196.850393701;
            if(precision == 0) {
                // Faster special case for integers
            	return Math.round(localValue) + unitString;
            } else {
                return String.format("%."+precision+"f%s", localValue, unitString);
            }
        }
	}

	/**
	 * Converts the input in local units into internal units
	 */
	public static double unSpeed(double local) {
		return local * (metric? KPH : MPH);
	}

	/**
	 * Converts the input in local units into internal units
	 */
	public static double unSpeed2(double local) {
		return local * (metric? 1 : FPM);
	}

    /**
     * Convert the bearing to a human readable format
     * @param degrees
     * @return "40° (N)"
     */
    public static String bearing1(double degrees) {
        if(Double.isNaN(degrees)) {
            return "";
        } else {
            if(degrees < 0) degrees += 360;
            String bearingStr = ((int) degrees) + "°";
            if(315 <= degrees || degrees < 45)
                return bearingStr + " (N)";
            else if(45 <= degrees && degrees < 135)
                return bearingStr + " (E)";
            else if(135 <= degrees && degrees < 225)
                return bearingStr + " (S)";
            else if(225 <= degrees && degrees < 315)
                return bearingStr + " (W)";
            else
                return bearingStr;
        }
    }
    
    /**
     * Convert the bearing to a human readable format, with more precision
     * @param degrees
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

    public static String latlong(double latitude, double longitude) {
        String lat = String.format(Locale.US, "%.6f%c", Math.abs(latitude), latitude < 0? 'S' : 'N');
        String lng = String.format(Locale.US, "%.6f%c", Math.abs(longitude), longitude < 0? 'W' : 'E');
        return lat + "," + lng;
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
     * Convert milliseconds to d:hh:mm:ss.sss
     * @param ms milliseconds
     * @return "m:ss.sss" or "h:mm:ss.sss"
     */
    public static String time2(long ms) {
        if(ms < 3600000) {
            // Less than 1 hour
            long min = ms / 60000;
            long sec = (ms / 1000) % 60;
            return String.format(Locale.US, "%d:%2d.%3d", min, sec, ms % 1000);
        } else if(ms < 86400000) {
            // Less than 1 day
            long hour = ms / 60000;
            long min = ms / 60000;
            long sec = (ms / 1000) % 60;
            return String.format(Locale.US, "%d:%2d:%2d.%3d", hour, min, sec, ms % 1000);
        } else {
            return String.format(Locale.US, "%s.%3d", DateFormat.format("kk:mm:ss", ms), ms % 1000);
        }
    }
    
    /**
     * Convert milliseconds to MmSSs
     * @param ms milliseconds
     * @return "MmSSs"
     */
    public static String time3(long ms) {
        long min = ms / 60000;
        long sec = (ms / 1000) % 60;
        if(min == 0)
        	return sec + "s";
        else if(sec < 10)
        	return min + "m0" + sec + "s";
        else
        	return min + "m" + sec + "s";
    }
    
    /**
     * Convert milliseconds to MmSS.Ss
     * @param ms milliseconds
     * @return "MmSS.Ss"
     */
    public static String time4(long ms) {
        long min = ms / 60000;
        long sec = (ms / 1000) % 60;
        long tenths = (ms / 100) % 10;
        String str1; // SS.Ss
        if(sec < 10)
        	str1 = "0" + sec + "." + tenths + "s";
        else
        	str1 = sec + "." + tenths + "s";
        // minutes
        if(min == 0)
        	return str1;
        else
        	return min + "m" + str1;
    }
    
    /**
     * Convert meters/second/second to local units (Gs)
     * @param mpss meters/second^2
     * @return G-force
     */
    public static String force(double mpss) {
        return String.format(Locale.US, "%.1fg", mpss / 9.80665);
    }
    public static String force(double mpss, boolean units) {
    	if(units)
    		return String.format(Locale.US, "%.1fg", mpss / 9.80665);
    	else
    		return String.format(Locale.US, "%.1f", mpss / 9.80665);
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
    
    /**
     * Converts a Location into a GeoPoint
     * @param loc The input Location
     * @return A GeoPoint representing loc
     */
    public static GeoPoint locToGeoPoint(Location loc) {
		return new GeoPoint((int)(loc.getLatitude() * 1E6), 
				             (int)(loc.getLongitude() * 1E6));
    }
    public static GeoPoint locToGeoPoint(double latitude, double longitude) {
		return new GeoPoint((int)(latitude * 1E6), 
				             (int)(longitude * 1E6));
    }

    public static void setMetric(boolean metric) {
    	Convert.metric = metric;
    }
    
}