package com.platypii.baseline.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are mathing correctly
 */
public class StatsTest {

    @Test
    public void stat() {
        Stat stat = new Stat();
        stat.addSample(10);
        stat.addSample(11);
        stat.addSample(12);

        assertEquals(11, stat.mean(), 0.1);
        assertEquals(0.66, stat.var(), 0.1);
        assertEquals("11.000 ± 0.667", stat.toString());
    }

    @Test
    public void empty() {
        Stat stat = new Stat();

        assertEquals(Double.NaN, stat.mean(), 0.1);
        assertEquals(Double.NaN, stat.var(), 0.1);
        assertEquals("NaN", stat.toString());
    }

}
