package com.platypii.baseline.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RangeTest {

    @Test
    public void isEmpty() {
        Range range = new Range();
        assertTrue(range.isEmpty());

        range.expand(3.14);
        assertFalse(range.isEmpty());
    }

    @Test
    public void expand() {
        Range range = new Range();
        range.expand(2.718);
        range.expand(3.14);

        assertEquals(2.718, range.min, 0.0001);
        assertEquals(3.14, range.max, 0.0001);
    }

    @Test
    public void stringify() {
        Range range = new Range();
        range.expand(3.14);
        range.expand(2.718);
        assertEquals("Range(2.718,3.14)", range.toString());
    }

}
