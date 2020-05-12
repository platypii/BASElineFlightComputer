package com.platypii.baseline.tracks;

import com.platypii.baseline.jarvis.FlightMode;
import com.platypii.baseline.measurements.MLocation;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    public void singleton() {
        final List<MLocation> points = Collections.singletonList(point(0, FlightMode.MODE_WINGSUIT));
        final TrackLabels labels = TrackLabels.from(points);
        assertNull(labels);
    }

    @Test
    public void labels() {
        final List<MLocation> points = Arrays.asList(
                point(0, FlightMode.MODE_GROUND),
                point(1000, FlightMode.MODE_GROUND),
                point(2000, FlightMode.MODE_WINGSUIT),
                point(3000, FlightMode.MODE_WINGSUIT),
                point(4000, FlightMode.MODE_WINGSUIT),
                point(5000, FlightMode.MODE_CANOPY),
                point(6000, FlightMode.MODE_CANOPY),
                point(7000, FlightMode.MODE_GROUND)
        );
        final TrackLabels labels = TrackLabels.from(points);
        assertNotNull(labels);
        assertEquals(2, labels.exit);
        assertEquals(4, labels.deploy);
        assertEquals(6, labels.land);
    }

    @Test
    public void canopyOnly() {
        final List<MLocation> points = Arrays.asList(
                point(7000, FlightMode.MODE_CANOPY),
                point(8000, FlightMode.MODE_CANOPY),
                point(9000, FlightMode.MODE_CANOPY)
        );
        final TrackLabels labels = TrackLabels.from(points);
        assertNotNull(labels);
        assertEquals(0, labels.exit);
        assertEquals(0, labels.deploy);
        assertEquals(2, labels.land);
    }

    /**
     * Generate a point with a given flight mode
     */
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