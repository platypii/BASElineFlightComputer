package com.platypii.baseline.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are bounding correctly
 */
public class BoundsTest {

    @Test
    public void expandBoundsOnce() {
        Bounds bounds = new Bounds();
        bounds.expandBounds(0, 0);

        assertEquals(0, bounds.x.min, 0.01);
        assertEquals(0, bounds.x.max, 0.01);
        assertEquals(0, bounds.y.min, 0.01);
        assertEquals(0, bounds.y.max, 0.01);
    }

    @Test
    public void expandBounds() {
        Bounds bounds = new Bounds();
        bounds.expandBounds(0, 0);
        bounds.expandBounds(1, 1);

        assertEquals(0, bounds.x.min, 0.01);
        assertEquals(1, bounds.x.max, 0.01);
        assertEquals(0, bounds.y.min, 0.01);
        assertEquals(1, bounds.y.max, 0.01);
    }

    @Test
    public void copyBounds() {
        Bounds bounds = new Bounds();
        bounds.expandBounds(1, 1);
        bounds.expandBounds(0, 0);

        Bounds copy = new Bounds();
        copy.set(bounds);

        assertEquals(0, copy.x.min, 0.01);
        assertEquals(1, copy.x.max, 0.01);
        assertEquals(0, copy.y.min, 0.01);
        assertEquals(1, copy.y.max, 0.01);
    }

    @Test
    public void resetBounds() {
        Bounds bounds = new Bounds();
        bounds.expandBounds(1, 1);
        bounds.expandBounds(0, 0);
        bounds.reset();

        assertEquals(Double.NaN, bounds.x.min, 0.01);
        assertEquals(Double.NaN, bounds.x.max, 0.01);
        assertEquals(Double.NaN, bounds.y.min, 0.01);
        assertEquals(Double.NaN, bounds.y.max, 0.01);
    }

}
