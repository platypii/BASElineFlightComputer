package com.platypii.baseline.location;

import com.platypii.baseline.util.Numbers;

public class LocationCheck {

    public static final int VALID = 0;
    public static final int UNLIKELY_LAT = 1;
    public static final int UNLIKELY_LON = 2;
    public static final int INVALID_ZERO = 3;
    public static final int INVALID_NAN = 4;
    public static final int INVALID_RANGE = 5;

    public static final String[] message = {
            "Valid",
            "Unlikely latitude",
            "Unlikely longitude",
            "Lat/long unlikely zero",
            "Lat/long not a number",
            "Lat/long out of bounds"
    };

    /**
     * Returns an error string if lat/long is invalid.
     * Returns null if seems valid
     */
    public static int validate(double latitude, double longitude) {
        if (Numbers.isReal(latitude) && Numbers.isReal(longitude)) {
            final double latitude_abs = Math.abs(latitude);
            final double longitude_abs = Math.abs(longitude);
            if (latitude_abs < 0.1 && longitude_abs < 0.1) {
                // If lat,lon == 0,0 assume bad data (there's no BASE off the coast of Africa)
                return INVALID_ZERO;
            } else if (latitude_abs > 180.0 || longitude_abs > 180.0) {
                // Lat/lon out of bounds. Likely parsing error.
                return INVALID_RANGE;
            } else if (latitude_abs < 0.1) {
                // No BASE jumps on the equator?
                return UNLIKELY_LAT;
            } else if (longitude_abs < 0.1 && latitude < 4) {
                // There is no landmass south of 4 degrees latitude on the prime meridian
                return UNLIKELY_LON;
            } else {
                return VALID;
            }
        } else {
            return INVALID_NAN;
        }
    }

}
