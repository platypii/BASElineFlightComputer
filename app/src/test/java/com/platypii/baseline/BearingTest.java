package com.platypii.baseline;

import com.platypii.baseline.data.measurements.MLocation;
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

        assertEquals(vN, -10, .1);
        assertEquals(vE, 17.3, .1);
    }

    @Test
    public void locationToBearing() {
        double vN = -10;
        double vE = 17.3;
        MLocation loc = new MLocation(0, 47, -122, 486, vN, vE, 0, 0, 0, 0, 0);

        assertEquals(loc.bearing(), 120, .1);
    }

    @Test
    public void locationToGroundSpeed() {
        double vN = -10;
        double vE = 17.3;
        MLocation loc = new MLocation(0, 47, -122, 486, vN, vE, 0, 0, 0, 0, 0);

        assertEquals(loc.groundSpeed(), 20, .1);
    }

}
