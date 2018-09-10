package com.platypii.baseline.location;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are parsing NMEA correctly
 */
public class LocationCheckTest {

    @Test
    public void validate() {
        assertEquals(LocationCheck.VALID, LocationCheck.validate(20, 30));
        // Equator
        assertEquals(LocationCheck.UNLIKELY_LAT, LocationCheck.validate(0, 30));
        // Prime meridian
        assertEquals(LocationCheck.UNLIKELY_LON, LocationCheck.validate(3, 0));
        assertEquals(LocationCheck.VALID, LocationCheck.validate(20, 0));
        assertEquals(LocationCheck.UNLIKELY_LON, LocationCheck.validate(60, 0));
        assertEquals(LocationCheck.INVALID_ZERO, LocationCheck.validate(0, 0));
        // Bounds
        assertEquals(LocationCheck.INVALID_RANGE, LocationCheck.validate(95, 30));
        assertEquals(LocationCheck.INVALID_RANGE, LocationCheck.validate(20, 200));
        // Keep it real
        assertEquals(LocationCheck.INVALID_NAN, LocationCheck.validate(20, Double.NaN));
        assertEquals(LocationCheck.INVALID_NAN, LocationCheck.validate(20, Double.POSITIVE_INFINITY));
        assertEquals(LocationCheck.INVALID_NAN, LocationCheck.validate(20, Double.NEGATIVE_INFINITY));
    }

}
