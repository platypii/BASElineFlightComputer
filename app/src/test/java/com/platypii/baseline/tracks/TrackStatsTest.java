package com.platypii.baseline.tracks;

import com.platypii.baseline.jarvis.FlightMode;
import com.platypii.baseline.measurements.MLocation;

import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TrackStatsTest {

    @Test
    public void trackStats() {
        final List<MLocation> points = new ArrayList<>();
        points.add(point(0, FlightMode.MODE_GROUND));
        points.add(point(1000, FlightMode.MODE_GROUND));
        points.add(point(2000, FlightMode.MODE_WINGSUIT));
        points.add(point(3000, FlightMode.MODE_WINGSUIT));
        points.add(point(4000, FlightMode.MODE_WINGSUIT));
        points.add(point(5000, FlightMode.MODE_CANOPY));
        points.add(point(6000, FlightMode.MODE_CANOPY));
        points.add(point(7000, FlightMode.MODE_GROUND));
        final TrackStats stats = new TrackStats(points);
        assertEquals(2000, stats.exit.millis);
        assertEquals(4000, stats.deploy.millis);
        assertEquals(6000, stats.land.millis);
        assertEquals(100, stats.altitude.min, 0.01);
        assertEquals(1000, stats.altitude.max, 0.01);
    }

    private MLocation point(long millis, int flightMode) {
        switch (flightMode) {
            case FlightMode.MODE_GROUND:
                return new MLocation(millis, 47, -123, 100, 0, 0, 0, 0, 0, 0, 0, 11, 11);
            case FlightMode.MODE_WINGSUIT:
                return new MLocation(millis, 47, -123, 1000, -20, 50, 20, 0, 0, 0, 0, 11, 11);
            case FlightMode.MODE_CANOPY:
                return new MLocation(millis, 47, -123, 500, -10, 5, 2, 0, 0, 0, 0, 11, 11);
            default:
                return new MLocation();
        }
    }
}