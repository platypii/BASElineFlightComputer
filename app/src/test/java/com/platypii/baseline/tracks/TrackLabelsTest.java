package com.platypii.baseline.tracks;

import com.platypii.baseline.jarvis.FlightMode;
import com.platypii.baseline.measurements.MLocation;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class TrackLabelsTest {

    @Test
    public void empty() {
        final List<MLocation> points = new ArrayList<>();
        final TrackLabels labels = TrackLabels.from(points);
        assertNull(labels);
    }

    @Test
    public void labelsFromPoints() {
        final List<MLocation> points = new ArrayList<>();
        points.add(point(0, FlightMode.MODE_GROUND));
        points.add(point(1000, FlightMode.MODE_GROUND));
        points.add(point(2000, FlightMode.MODE_WINGSUIT));
        points.add(point(3000, FlightMode.MODE_WINGSUIT));
        points.add(point(4000, FlightMode.MODE_WINGSUIT));
        points.add(point(5000, FlightMode.MODE_CANOPY));
        points.add(point(6000, FlightMode.MODE_CANOPY));
        points.add(point(7000, FlightMode.MODE_GROUND));
        final TrackLabels labels = TrackLabels.from(points);
        assertNotNull(labels);
        assertEquals(2, labels.exit);
        assertEquals(4, labels.deploy);
        assertEquals(6, labels.land);
    }

    @NonNull
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