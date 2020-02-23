package com.platypii.baseline.lasers;

import com.platypii.baseline.places.Place;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LaserSearchTest {

    @Test
    public void matchLaser() {
        final LaserProfile laser = new LaserProfile("lid", "uid", "LaserName", true, 0.0, 1.0, 2.0, "test", null);
        assertTrue(LaserSearch.matchLaser(laser, ""));
        assertTrue(LaserSearch.matchLaser(laser, " "));
        assertFalse(LaserSearch.matchLaser(laser, "BASE"));
        assertFalse(LaserSearch.matchLaser(laser, "Skydive"));
        assertTrue(LaserSearch.matchLaser(laser, "LaserNa"));
        assertTrue(LaserSearch.matchLaser(laser, "LaserName"));
        assertTrue(LaserSearch.matchLaser(laser, "laser name"));
        assertTrue(LaserSearch.matchLaser(laser, "la na"));
        assertFalse(LaserSearch.matchLaser(laser, "laz"));
    }

    @Test
    public void matchLaserWithPlace() {
        final LaserProfile laser = new LaserProfile("lid", "uid", "name", true, 0.0, 1.0, 2.0, "test", null);
        assertFalse(LaserSearch.matchLaser(laser, "Norway"));
        laser.place = new Place("Fjord", "", "Norway", 68.165,16.593, 1364, "E", 1000, true);
        assertTrue(LaserSearch.matchLaser(laser, "Fjord"));
        assertTrue(LaserSearch.matchLaser(laser, "Norway"));
        assertTrue(LaserSearch.matchLaser(laser, "Fjord Norway"));
        assertTrue(LaserSearch.matchLaser(laser, "fjo no"));
    }

}
