package com.platypii.baseline.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are converting correctly
 */
public class ConvertTest {

    @Test
    public void convertAltitude() {
        assertEquals("0 ft", Convert.altitude(0.0));
        assertEquals("3.3 kft", Convert.altitude(1000.0));
        assertEquals("", Convert.altitude(Double.NaN));
        assertEquals("Infinity", Convert.altitude(Double.POSITIVE_INFINITY));
    }

    @Test
    public void convertDistance() {
        assertEquals("0 ft", Convert.distance(0.0));
        assertEquals("3 ft", Convert.distance(1.0));
        assertEquals("3.3 ft", Convert.distance(1.0, 1, true));
        assertEquals("3.3", Convert.distance(1.0, 1, false));
        assertEquals("", Convert.distance(Double.NaN));
        assertEquals("Infinity", Convert.distance(Double.POSITIVE_INFINITY));
    }

    @Test
    public void convertDistance2() {
        assertEquals("3200 feet", Convert.distance2(1000, 2));
        assertEquals("3280 feet", Convert.distance2(1000, 3));
        assertEquals("1 mile", Convert.distance2(1609.34, 1));
        assertEquals("1.0 miles", Convert.distance2(1609.34, 2));
        assertEquals("", Convert.distance2(Double.NaN, 1));
        assertEquals("Infinity", Convert.distance2(Double.POSITIVE_INFINITY, 1));
        assertEquals("-Infinity", Convert.distance2(Double.NEGATIVE_INFINITY, 1));
    }

    @Test
    public void convertDistance2Metric() {
        Convert.metric = true;
        assertEquals("990 meters", Convert.distance2(999, 2));
        assertEquals("999 meters", Convert.distance2(999, 3));
        assertEquals("1 kilometer", Convert.distance2(1000, 1));
        assertEquals("1.0 kilometers", Convert.distance2(1000, 2));
    }

    @Test
    public void convertDistance3() {
        assertEquals("3 ft", Convert.distance3(1));
        assertEquals("3281 ft", Convert.distance3(1000));
        assertEquals("1 mi", Convert.distance3(1609.34));
        assertEquals("10 mi", Convert.distance3(16093.4));
        assertEquals("", Convert.distance3(Double.NaN));
        assertEquals("Infinity", Convert.distance3(Double.POSITIVE_INFINITY));
        assertEquals("-Infinity", Convert.distance3(Double.NEGATIVE_INFINITY));
    }

    @Test
    public void convertDistance3Metric() {
        Convert.metric = true;
        assertEquals("1 m", Convert.distance3(1));
        assertEquals("1 km", Convert.distance3(1000));
        assertEquals("2 km", Convert.distance3(1609.34));
        assertEquals("16 km", Convert.distance3(16093.4));
    }

    @Test
    public void convertSpeed() {
        assertEquals("0.0 mph", Convert.speed(0.0));
        assertEquals("9.9 mph", Convert.speed(9.9 * Convert.MPH));
        assertEquals("22 mph", Convert.speed(10.0));
        assertEquals("224 mph", Convert.speed(100.0));
        assertEquals("224 mph", Convert.speed(100.0, 0, true));
        assertEquals("224", Convert.speed(100.0, 0, false));
        assertEquals("", Convert.speed(Double.NaN));
        assertEquals("Infinity", Convert.speed(Double.POSITIVE_INFINITY));
    }

    @Test
    public void convertGlide() {
        assertEquals("0.0", Convert.glide(0.0, 1, false));
        assertEquals("0.0 : 1", Convert.glide(0.0, 1, true));
        assertEquals("1.0 : 1", Convert.glide(1.0, 1, true));
        assertEquals("2.0 : 1", Convert.glide(2.0, 1, true));
        assertEquals("+2.0 : 1", Convert.glide(-2.0, 1, true));

        assertEquals("2.0", Convert.glide(20, -10, 1, false));
        assertEquals("2.0 : 1", Convert.glide(20, -10, 1, true));
        assertEquals("+2.0 : 1", Convert.glide(20, 10, 1, true));

        // Special cases
        assertEquals("", Convert.glide(0, 0, 1, true));
        assertEquals("", Convert.glide(Double.NaN, 1, true));
        assertEquals("Level", Convert.glide(Double.POSITIVE_INFINITY, 1, true));
        assertEquals("Level", Convert.glide(41, 1, true));
        assertEquals("Level", Convert.glide(20, 0, 1, true));
        assertEquals("Vertical", Convert.glide(0, 10, 1, true));
        assertEquals("Stationary", Convert.glide(0.1, 0.1, 1, true));
    }

    @Test
    public void convertGlide2() {
        assertEquals("2.0", Convert.glide2(20, -10, 1, false));
        assertEquals("2.0 : 1", Convert.glide2(20, -10, 1, true));
        assertEquals("+2.0 : 1", Convert.glide2(20, 10, 1, true));

        // Special cases
        assertEquals("", Convert.glide2(20, 0, 1, true));
        assertEquals("", Convert.glide2(0, 10, 1, true));
        assertEquals("", Convert.glide2(0.1, 0.1, 1, true));
        assertEquals("", Convert.glide2(0, 0, 1, true));
    }

    @Test
    public void convertPressure() {
        assertEquals("900.00 hPa", Convert.pressure(900.0));
        assertEquals("", Convert.pressure(Double.NaN));
    }

    @Test
    public void convertAngle() {
        assertEquals("0°", Convert.angle(0.0));
        assertEquals("90°", Convert.angle(90.0));
        assertEquals("90°", Convert.angle(90.5));
        assertEquals("135°", Convert.angle(135.0));
        assertEquals("", Convert.angle(Double.NaN));
    }

    @Test
    public void convertAngle2() {
        assertEquals("straight", Convert.angle2(0.0));
        assertEquals("90 right", Convert.angle2(90.0));
        assertEquals("90 right", Convert.angle2(90.5));
        assertEquals("130 right", Convert.angle2(135.0));
        assertEquals("130 left", Convert.angle2(-135.0));
        assertEquals("", Convert.angle2(Double.NaN));
    }

    @Test
    public void convertBearing2() {
        assertEquals("0° (N)", Convert.bearing2(0.0));
        assertEquals("45° (NE)", Convert.bearing2(45.0));
        assertEquals("90° (E)", Convert.bearing2(90.0));
        assertEquals("90° (E)", Convert.bearing2(90.5));
        assertEquals("135° (SE)", Convert.bearing2(135.0));
        assertEquals("188° (S)", Convert.bearing2(188.0));
        assertEquals("225° (SW)", Convert.bearing2(-135.0));
        assertEquals("270° (W)", Convert.bearing2(-90.0));
        assertEquals("300° (NW)", Convert.bearing2(660.0));
        assertEquals("", Convert.bearing2(Double.NaN));
    }

    @Test
    public void kts2mps() {
        assertEquals(5.144, Convert.kts2mps(10.0), 0.1);
    }

}
