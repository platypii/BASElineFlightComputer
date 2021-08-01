package com.platypii.baseline.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are converting correctly
 */
public class ConvertUtilTest {

    @Test
    public void convertFormatDouble() {
        assertEquals("0", ConvertUtil.formatDouble(0.0, 2));
        assertEquals("1.0", ConvertUtil.formatDouble(1.0, 2));

        assertEquals("9", ConvertUtil.formatDouble(9.8, 0));
        assertEquals("9", ConvertUtil.formatDouble(9.8, 1));
        assertEquals("9.8", ConvertUtil.formatDouble(9.8, 2));
        assertEquals("9.80", ConvertUtil.formatDouble(9.8, 3));

        assertEquals("10", ConvertUtil.formatDouble(10, 1));
        assertEquals("10", ConvertUtil.formatDouble(10, 2));
        assertEquals("10.0", ConvertUtil.formatDouble(10, 3));

        assertEquals("3200", ConvertUtil.formatDouble(3280, 2));
        assertEquals("3280", ConvertUtil.formatDouble(3280, 3));
        assertEquals("3280", ConvertUtil.formatDouble(3280, 4));
        assertEquals("3280.0", ConvertUtil.formatDouble(3280, 5));
        assertEquals("3280.00", ConvertUtil.formatDouble(3280, 6));

        assertEquals("-9", ConvertUtil.formatDouble(-9.8, 1));
        assertEquals("-9.8", ConvertUtil.formatDouble(-9.8, 2));
        assertEquals("-9.80", ConvertUtil.formatDouble(-9.8, 3));

        assertEquals("9.2", ConvertUtil.formatDouble(9.2, 2));
        assertEquals(".92", ConvertUtil.formatDouble(0.92, 2));
        assertEquals(".092", ConvertUtil.formatDouble(0.092, 2));
        assertEquals(".0092", ConvertUtil.formatDouble(0.0092, 2));

        assertEquals(".098", ConvertUtil.formatDouble(0.098, 2));
        assertEquals(".99", ConvertUtil.formatDouble(0.999, 2));

        assertEquals("NaN", ConvertUtil.formatDouble(Double.NaN, 2));
        assertEquals("Infinity", ConvertUtil.formatDouble(Double.POSITIVE_INFINITY, 2));
        assertEquals("-Infinity", ConvertUtil.formatDouble(Double.NEGATIVE_INFINITY, 2));
    }

    @Test
    public void convertFormatInt() {
        assertEquals("9", ConvertUtil.formatInt(9.8, 0));
        assertEquals("9", ConvertUtil.formatInt(9.8, 1));
        assertEquals("9", ConvertUtil.formatInt(9.8, 2));
        assertEquals("9", ConvertUtil.formatInt(9.8, 3));

        assertEquals("90", ConvertUtil.formatInt(98.6, 1));
        assertEquals("98", ConvertUtil.formatInt(98.6, 2));
        assertEquals("98", ConvertUtil.formatInt(98.6, 3));

        assertEquals("-9", ConvertUtil.formatInt(-9.8, 1));
        assertEquals("-9", ConvertUtil.formatInt(-9.8, 2));
        assertEquals("-9", ConvertUtil.formatInt(-9.8, 3));
    }

}
