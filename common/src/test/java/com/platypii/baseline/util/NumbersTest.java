package com.platypii.baseline.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are mathing correctly
 */
public class NumbersTest {

    @Test
    public void pow() {
//        assertEquals(1, Numbers.pow(0, 0)); // undefined
        assertEquals(0, Numbers.pow(0, 1));
        assertEquals(0, Numbers.pow(0, 2));
        assertEquals(1, Numbers.pow(1, 0));
        assertEquals(1, Numbers.pow(1, 1));
        assertEquals(1, Numbers.pow(1, 2));
        assertEquals(1, Numbers.pow(2, 0));
        assertEquals(2, Numbers.pow(2, 1));
        assertEquals(4, Numbers.pow(2, 2));
        assertEquals(8, Numbers.pow(2, 3));
        assertEquals(16, Numbers.pow(2, 4));
        assertEquals(32, Numbers.pow(2, 5));
        assertEquals(256, Numbers.pow(2, 8));
        assertEquals(65536, Numbers.pow(2, 16));
        assertEquals(1, Numbers.pow(10, 0));
        assertEquals(10, Numbers.pow(10, 1));
        assertEquals(100, Numbers.pow(10, 2));
        assertEquals(1, Numbers.pow(-1, 0));
        assertEquals(-1, Numbers.pow(-1, 1));
        assertEquals(1, Numbers.pow(-1, 2));
        assertEquals(1, Numbers.pow(-2, 0));
        assertEquals(-2, Numbers.pow(-2, 1));
        assertEquals(4, Numbers.pow(-2, 2));
    }

    @Test
    public void parseInt() {
        assertEquals(-2, Numbers.parseInt("-2", -1));
        assertEquals(-1, Numbers.parseInt("-1", -1));
        assertEquals(0, Numbers.parseInt("0", -1));
        assertEquals(1, Numbers.parseInt("1", -1));
        assertEquals(2, Numbers.parseInt("2", -1));
        assertEquals(-1, Numbers.parseInt("", -1));
//        assertEquals(-1, Numbers.parseInt("0.0", -1));
//        assertEquals(-1, Numbers.parseInt("0.1", -1));
        assertEquals(-1, Numbers.parseInt("X", -1));
        assertEquals(-1, Numbers.parseInt(null, -1));
    }

    @Test
    public void parseFloat() {
        assertEquals(-2, Numbers.parseFloat("-2"), 0.1);
        assertEquals(-1, Numbers.parseFloat("-1"), 0.1);
        assertEquals(0, Numbers.parseFloat("0"), 0.1);
        assertEquals(1, Numbers.parseFloat("1"), 0.1);
        assertEquals(2, Numbers.parseFloat("2"), 0.1);
        assertEquals(Float.NaN, Numbers.parseFloat(""), 0.1);
        assertEquals(0.0, Numbers.parseFloat("0.0"), 0.1);
        assertEquals(0.1, Numbers.parseFloat("0.1"), 0.1);
        assertEquals(Float.NaN, Numbers.parseFloat("X"), 0.1);
        assertEquals(Float.NaN, Numbers.parseFloat("NaN"), 0.1);
        assertEquals(Float.NaN, Numbers.parseFloat(null), 0.1);
    }

    @Test
    public void parseDouble() {
        assertEquals(-2, Numbers.parseDouble("-2"), 0.1);
        assertEquals(-1, Numbers.parseDouble("-1"), 0.1);
        assertEquals(0, Numbers.parseDouble("0"), 0.1);
        assertEquals(1, Numbers.parseDouble("1"), 0.1);
        assertEquals(2, Numbers.parseDouble("2"), 0.1);
        assertEquals(Double.NaN, Numbers.parseDouble(""), 0.1);
        assertEquals(0.0, Numbers.parseDouble("0.0"), 0.1);
        assertEquals(0.1, Numbers.parseDouble("0.1"), 0.1);
        assertEquals(Double.NaN, Numbers.parseDouble("X"), 0.1);
        assertEquals(Double.NaN, Numbers.parseDouble("NaN"), 0.1);
        assertEquals(Double.NaN, Numbers.parseDouble(null), 0.1);
    }

}
