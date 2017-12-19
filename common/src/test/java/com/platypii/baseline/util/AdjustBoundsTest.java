package com.platypii.baseline.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are bounding correctly
 */
public class AdjustBoundsTest {

    @Test
    public void cleanBounds() {
        Bounds bounds = new Bounds();
        Bounds min = new Bounds();
        min.set(-1,1,1,-1);
        Bounds max = new Bounds();
        max.set(-2,2,2,-2);

        AdjustBounds.clean(bounds, min, max);

        assertEquals( -1, bounds.x.min, 0.1);
        assertEquals( 1, bounds.y.max, 0.1);
        assertEquals( 1, bounds.x.max, 0.1);
        assertEquals( -1, bounds.y.min, 0.1);
    }

}
