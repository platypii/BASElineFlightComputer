package com.platypii.baseline.jarvis;

import com.platypii.baseline.measurements.MLocation;

import androidx.annotation.NonNull;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FlightModeTest {

    @Test
    public void detectFlightMode() {
        assertEquals("Ground", getMode(getLoc(0, 0)));
        assertEquals("Ground", getMode(getLoc(1, 10)));
        assertEquals("Plane", getMode(getLoc(0, 100)));
        assertEquals("Wingsuit", getMode(getLoc(-20, 50)));
        assertEquals("Wingsuit", getMode(getLoc(-18, 14)));
        assertEquals("Freefall", getMode(getLoc(-40, 0)));
        assertEquals("Canopy", getMode(getLoc(-5, 20)));
        assertEquals("", getMode(getLoc(15, 20)));
    }

    @NonNull
    private MLocation getLoc(double climb, double groundspeed) {
        return new MLocation(0L, 0.0, 0.0, 0.0, climb, groundspeed, 0.0, 0f, 0f, 0f, 0f, 0, 0);
    }

    private String getMode(@NonNull MLocation loc) {
        return FlightMode.getModeString(FlightMode.getMode(loc));
    }

}
