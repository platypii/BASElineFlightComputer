package com.platypii.baseline.lasers;

import com.platypii.baseline.places.Place;

import org.junit.Test;

import static com.platypii.baseline.lasers.LaserSearch.matchLaser;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LaserSearchTest {

    @Test
    public void matchLasers() {
        final LaserProfile laser = new LaserProfile("lid", "uid", "LaserName", true, 0.0, 1.0, 2.0, "test", null);
        assertTrue(matchLaser(laser, ""));
        assertTrue(matchLaser(laser, " "));
        assertFalse(matchLaser(laser, "BASE"));
        assertFalse(matchLaser(laser, "Skydive"));
        assertTrue(matchLaser(laser, "LaserNa"));
        assertTrue(matchLaser(laser, "LaserName"));
        assertTrue(matchLaser(laser, "laser name"));
        assertTrue(matchLaser(laser, "la na"));
        assertFalse(matchLaser(laser, "laz"));
        assertTrue(matchLaser(laser, "LÁserNÁmÊ"));
    }

    @Test
    public void matchLasersWithPlace() {
        final LaserProfile laser = new LaserProfile("lid", "uid", "name", true, 0.0, 1.0, 2.0, "test", null);
        assertFalse(matchLaser(laser, "Norway"));
        laser.place = new Place("Fjord", "", "Norway", 68.165,16.593, 1364, "E", 1000, true);
        assertTrue(matchLaser(laser, "Fjord"));
        assertTrue(matchLaser(laser, "Norway"));
        assertTrue(matchLaser(laser, "Fjord Norway"));
        assertTrue(matchLaser(laser, "fjo no"));
    }

}
