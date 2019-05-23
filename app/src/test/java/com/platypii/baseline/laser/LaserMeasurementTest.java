package com.platypii.baseline.laser;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LaserMeasurementTest {

    @Test
    public void parse() throws ParseException {
        assertEquals(1, LaserMeasurement.parse("100,-100", true, true).size());
        assertEquals(2, LaserMeasurement.parse("100,-100\n50.0, -50.0", true, true).size());
        assertEquals(2, LaserMeasurement.parse("100,-100\n50.0, -50.0\n", true, true).size());
        assertEquals(1, LaserMeasurement.parse("100,-100,", true, false).size());
        assertEquals(0, LaserMeasurement.parse("100,-100,22", true, false).size());
        assertEquals(0, LaserMeasurement.parse("100,z", true, false).size());
    }

    @Test
    public void render() {
        List<LaserMeasurement> points = testPoints();
        // Metric
        assertEquals("100.0, -100.0\n", LaserMeasurement.render(points, true).toString());
        // Imperial
        assertEquals("328.1, -328.1\n", LaserMeasurement.render(points, false).toString());
    }

    @Test
    public void reorder() {
        List<LaserMeasurement> points = testPoints();
        points.add(new LaserMeasurement(50, -50));
        assertEquals("100.0, -100.0\n50.0, -50.0\n", LaserMeasurement.render(points, true).toString());

        // Sort quadrant 2
        List<LaserMeasurement> reordered = LaserMeasurement.reorder(points);
        assertEquals("50.0, -50.0\n100.0, -100.0\n", LaserMeasurement.render(reordered, true).toString());
    }

    private List<LaserMeasurement> testPoints() {
        List<LaserMeasurement> points = new ArrayList<>();
        points.add(new LaserMeasurement(100,-100));
        return points;
    }

}
