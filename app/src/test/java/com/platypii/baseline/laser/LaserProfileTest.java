package com.platypii.baseline.laser;

import com.platypii.baseline.util.Convert;
import com.google.gson.Gson;
import java.util.ArrayList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LaserProfileTest {

    private final LaserProfile laser = new LaserProfile(
            "laser_id",
            "user_id",
            "Laser Name",
            true,
            0.0,
            47.24,
            -123.14,
            "source",
            new ArrayList<>()
    );

    @Test
    public void json() {
        final Gson gson = new Gson();
        final String serialized = gson.toJson(laser);
        final String expected = "{\"laser_id\":\"laser_id\",\"user_id\":\"user_id\",\"name\":\"Laser Name\",\"public\":true,\"alt\":0.0,\"lat\":47.24,\"lng\":-123.14,\"source\":\"source\",\"points\":[]}";
        assertEquals(expected, serialized);
        final LaserProfile parsed = gson.fromJson(expected, LaserProfile.class);
        assertEquals(laser, parsed);
    }

    @Test
    public void locationString() {
        Convert.metric = false;
        assertEquals("47.24, -123.14, 0 ft", laser.locationString());
    }

    @Test
    public void laserProfileToString() {
        assertEquals("LaserProfile(laser_id, Laser Name)", laser.toString());
    }
}
