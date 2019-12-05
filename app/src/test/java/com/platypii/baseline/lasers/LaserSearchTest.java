package com.platypii.baseline.lasers;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LaserSearchTest {

    @Test
    public void matchLaser() {
        assertTrue(LaserSearch.matchLaser(new LaserProfile("lid", "uid", "name", true, 0.0, 1.0, 2.0, "test", null), ""));
        assertTrue(LaserSearch.matchLaser(new LaserProfile("lid", "uid", "name", true, 0.0, 1.0, 2.0, "test", null), " "));
        assertFalse(LaserSearch.matchLaser(new LaserProfile("lid", "uid", "name", true, 0.0, 1.0, 2.0, "test", null), "BASE"));
        assertFalse(LaserSearch.matchLaser(new LaserProfile("lid", "uid", "name", true, 0.0, 1.0, 2.0, "test", null), "Skydive"));
        assertTrue(LaserSearch.matchLaser(new LaserProfile("lid", "uid", "LaserName", true, 0.0, 1.0, 2.0, "test", null), "LaserName"));
    }

}
