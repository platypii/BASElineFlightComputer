package com.platypii.baseline;

import com.platypii.baseline.measurements.MLocation;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are converting bearing correctly
 */
public class BearingTest {

    @Test
    public void bearingToVelocityComponents() {
        double bearing = 120;
        double groundSpeed = 20;

        double vN = groundSpeed * Math.cos(Math.toRadians(bearing));
        double vE = groundSpeed * Math.sin(Math.toRadians(bearing));

        assertEquals(-10, vN, .1);
        assertEquals(17.3, vE, .1);
    }

    @Test
    public void locationToBearing() {
        double vN = -10;
        double vE = 17.3;
        MLocation loc = new MLocation(0, 47, -122, 486, 0, vN, vE, 0, 0, 0, 0, 0, 0);

        assertEquals(120, loc.bearing(), .1);
    }

    @Test
    public void locationToGroundSpeed() {
        double vN = -10;
        double vE = 17.3;
        MLocation loc = new MLocation(0, 47, -122, 486, 0, vN, vE, 0, 0, 0, 0, 0, 0);

        assertEquals(20, loc.groundSpeed(), .1);
    }

}
