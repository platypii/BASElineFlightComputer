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

        assertEquals( 0, bounds.x.min, 0.1);
        assertEquals( 0, bounds.y.max, 0.1);
        assertEquals( 0, bounds.x.max, 0.1);
        assertEquals( 0, bounds.y.min, 0.1);
    }

}
