package com.platypii.baseline.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are converting correctly
 */
public class ConvertTest {

    @Test
    public void convertFormatDouble() {
        assertEquals( "0", Convert.formatDouble(0.0, 2));
        assertEquals( "1.0", Convert.formatDouble(1.0, 2));

        assertEquals( "9", Convert.formatDouble(9.8, 0));
        assertEquals( "9", Convert.formatDouble(9.8, 1));
        assertEquals( "9.8", Convert.formatDouble(9.8, 2));
        assertEquals( "9.80", Convert.formatDouble(9.8, 3));

        assertEquals( "10", Convert.formatDouble(10, 1));
        assertEquals( "10", Convert.formatDouble(10, 2));
        assertEquals( "10.0", Convert.formatDouble(10, 3));

        assertEquals( "3200", Convert.formatDouble(3280, 2));
        assertEquals( "3280", Convert.formatDouble(3280, 3));
        assertEquals( "3280", Convert.formatDouble(3280, 4));
        assertEquals( "3280.0", Convert.formatDouble(3280, 5));
        assertEquals( "3280.00", Convert.formatDouble(3280, 6));

        assertEquals( "-9", Convert.formatDouble(-9.8, 1));
        assertEquals( "-9.8", Convert.formatDouble(-9.8, 2));
        assertEquals( "-9.80", Convert.formatDouble(-9.8, 3));

        assertEquals( "9.2", Convert.formatDouble(9.2, 2));
        assertEquals( ".92", Convert.formatDouble(0.92, 2));
        assertEquals( ".092", Convert.formatDouble(0.092, 2));
        assertEquals( ".0092", Convert.formatDouble(0.0092, 2));

        assertEquals( ".098", Convert.formatDouble(0.098, 2));
        assertEquals( ".99", Convert.formatDouble(0.999, 2));

        assertEquals( "NaN", Convert.formatDouble(Double.NaN, 2));
        assertEquals( "Infinity", Convert.formatDouble(Double.POSITIVE_INFINITY, 2));
        assertEquals( "-Infinity", Convert.formatDouble(Double.NEGATIVE_INFINITY, 2));
    }

    @Test
    public void convertFormatInt() {
        assertEquals( "9", Convert.formatInt(9.8, 1));
        assertEquals( "9", Convert.formatInt(9.8, 2));
        assertEquals( "9", Convert.formatInt(9.8, 3));

        assertEquals( "90", Convert.formatInt(98.6, 1));
        assertEquals( "98", Convert.formatInt(98.6, 2));
        assertEquals( "98", Convert.formatInt(98.6, 3));

        assertEquals( "-9", Convert.formatInt(-9.8, 1));
        assertEquals( "-9", Convert.formatInt(-9.8, 2));
        assertEquals( "-9", Convert.formatInt(-9.8, 3));
    }

    @Test
    public void convertDistance2() {
        assertEquals( "3200 feet", Convert.distance2(1000, 2, true));
        assertEquals( "3280 feet", Convert.distance2(1000, 3, true));
        assertEquals( "1.0 miles", Convert.distance2(1609.34, 2, true));
    }

    @Test
    public void convertDistance2Metric() {
        Convert.metric = true;
        assertEquals( "1.0 kilometers", Convert.distance2(1000, 2, true));
        assertEquals( "990 meters", Convert.distance2(999, 2, true));
        assertEquals( "999 meters", Convert.distance2(999, 3, true));
    }

}
