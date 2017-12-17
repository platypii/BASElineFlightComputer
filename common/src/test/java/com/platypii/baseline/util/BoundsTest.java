package com.platypii.baseline.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are bounding correctly
 */
public class BoundsTest {

    @Test
    public void expandBounds() {
        Bounds bounds = new Bounds();

        bounds.expandBounds(0,0);

        assertEquals( 0, bounds.left, 0.1);
        assertEquals( 0, bounds.top, 0.1);
        assertEquals( 0, bounds.right, 0.1);
        assertEquals( 0, bounds.bottom, 0.1);
    }

    @Test
    public void cleanBounds() {
        Bounds bounds = new Bounds();
        Bounds min = new Bounds();
        min.set(-1,1,1,-1);
        Bounds max = new Bounds();
        max.set(-2,2,2,-2);

        bounds.clean(min, max);

        assertEquals( -1, bounds.left, 0.1);
        assertEquals( 1, bounds.top, 0.1);
        assertEquals( 1, bounds.right, 0.1);
        assertEquals( -1, bounds.bottom, 0.1);
    }

}
