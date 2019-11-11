package com.platypii.baseline.location;

import com.platypii.baseline.measurements.LatLngAlt;
import com.platypii.baseline.util.Numbers;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.text.ParseException;

public class Geocoder {

    private static final String defaultError = "Invalid latitude, longitude, altitude";

    /**
     * Takes in a lat,lon,alt string and returns an object
     *
     * @throws ParseException for invalid input
     */
    @NonNull
    public static LatLngAlt parse(@Nullable String str) throws ParseException {
        if (str == null || str.trim().isEmpty()) {
            throw new ParseException("Missing latitude, longitude, altitude", 0);
        }
        final String[] split = str.split(",");
        if (split.length < 2 || split.length > 3) {
            throw new ParseException(defaultError, 0);
        }

        // Latitude
        if (split[0].trim().isEmpty()) {
            if (split.length == 3) {
                throw new ParseException("Missing latitude", 0);
            } else {
                throw new ParseException(defaultError, 0);
            }
        }
        final Double lat = Numbers.parseDoubleNull(split[0]);
        if (lat == null) {
            throw new ParseException("Invalid latitude: " + split[0].trim(), 0);
        }
        if (lat < -90 || lat > 90) {
            throw new ParseException("Invalid latitude: " + split[0].trim() + " is not between -90 and 90", 0);
        }

        // Longitude
        if (split[1].trim().isEmpty()) {
            if (split.length == 2) {
                throw new ParseException("Missing longitude, altitude", 0);
            } else {
                throw new ParseException("Missing longitude", 0);
            }
        }
        final Double lng = Numbers.parseDoubleNull(split[1]);
        if (lng == null) {
            throw new ParseException("Invalid longitude: " + split[1].trim(), 0);
        }
        if (lng < -180 || lng > 180) {
            throw new ParseException("Invalid longitude: " + split[1].trim() + " is not between -180 and 180", 0);
        }

        // Altitude
        if (split.length == 2 || split[2].trim().isEmpty()) {
            throw new ParseException("Missing altitude", 0);
        }
        final String altStr = split[2].trim();
        final Double alt;
        if (altStr.endsWith("ft")) {
            final Double feet = Numbers.parseDoubleNull(altStr.substring(0, altStr.length() - 2));
            alt = feet == null ? null : feet * 0.3048;
        } else if (altStr.endsWith("m")) {
            alt = Numbers.parseDoubleNull(altStr.substring(0, altStr.length() - 1));
        } else {
            alt = Numbers.parseDoubleNull(altStr);
        }
        if (alt == null) {
            throw new ParseException("Invalid altitude: " + split[2].trim(), 0);
        }
        return new LatLngAlt(lat, lng, alt);
    }
}
