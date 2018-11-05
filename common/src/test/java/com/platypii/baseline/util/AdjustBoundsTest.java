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
        min.set(-1, 1, 1, -1);
        Bounds max = new Bounds();
        max.set(-2, 2, 2, -2);

        AdjustBounds.clean(bounds, min, max);

        assertEquals(-1, bounds.x.min, 0.01);
        assertEquals(1, bounds.y.max, 0.01);
        assertEquals(1, bounds.x.max, 0.01);
        assertEquals(-1, bounds.y.min, 0.01);
    }

    @Test
    public void squareBoundsLandscape() {
        Bounds bounds = new Bounds();
        bounds.set(0, 1, 1, 0);
        IntBounds padding = new IntBounds();

        AdjustBounds.squareBounds(bounds, 400, 300, padding);

        assertEquals(0, bounds.x.min, 0.01);
        assertEquals(1, bounds.y.max, 0.01);
        assertEquals(1.33, bounds.x.max, 0.01);
        assertEquals(0, bounds.y.min, 0.01);
    }

    @Test
    public void squareBoundsPortrait() {
        Bounds bounds = new Bounds();
        bounds.set(0, 1, 1, 0);
        IntBounds padding = new IntBounds();

        AdjustBounds.squareBounds(bounds, 300, 400, padding);

        assertEquals(0, bounds.x.min, 0.01);
        assertEquals(1, bounds.y.max, 0.01);
        assertEquals(1, bounds.x.max, 0.01);
        assertEquals(-0.33, bounds.y.min, 0.01);
    }

}
