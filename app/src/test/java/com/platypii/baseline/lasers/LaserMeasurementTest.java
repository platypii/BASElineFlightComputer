package com.platypii.baseline.lasers;

import androidx.annotation.NonNull;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LaserMeasurementTest {

    @Test
    public void parseMetric() throws ParseException {
        assertEquals(0, LaserMeasurement.parse("", true, false).size());
        assertEquals(0, LaserMeasurement.parse(" ", true, false).size());
        assertEquals(0, LaserMeasurement.parse("\n", true, false).size());
        assertEquals(0, LaserMeasurement.parse("\n\n", true, false).size());
        assertEquals(1, LaserMeasurement.parse("100 -100", true, true).size());
        assertEquals(1, LaserMeasurement.parse("100,-100", true, true).size());
        assertEquals(1, LaserMeasurement.parse("100, -100", true, true).size());
        assertEquals(1, LaserMeasurement.parse("100\t-100", true, true).size());
        assertEquals(1, LaserMeasurement.parse("100/-100", true, true).size());
        assertEquals(1, LaserMeasurement.parse("  100,  -100  ", true, true).size());
        assertEquals(2, LaserMeasurement.parse("100,-100\n20.0, -50.0", true, true).size());
        assertEquals(2, LaserMeasurement.parse("100,-100\n20.0, -50.0\n", true, true).size());
        assertEquals(2, LaserMeasurement.parse("100 -100\n20.0 -50.0\n", true, true).size());
        assertEquals(2, LaserMeasurement.parse("100 -100\n  20.0  -50.0  ", true, true).size());
    }

    @Test
    public void parseFreedom() throws ParseException {
        assertEquals(1, LaserMeasurement.parse("100 200", false, true).size());
        assertEquals(2, LaserMeasurement.parse("100 200\n20.0 -50.0", false, true).size());
    }

    @Test
    public void parseWithUnits() throws ParseException {
        assertEquals(100, LaserMeasurement.parse("100 m, -100 m", true, true).get(0).x, 0.01);
        assertEquals(100, LaserMeasurement.parse("100 m, -100 m", false, true).get(0).x, 0.01);
        assertEquals(30.48, LaserMeasurement.parse("100 ft, -100 ft", true, true).get(0).x, 0.01);
        assertEquals(30.48, LaserMeasurement.parse("100 ft, -100 ft", false, true).get(0).x, 0.01);
        assertEquals(91.44, LaserMeasurement.parse("100 yd, -100 yd", true, true).get(0).x, 0.01);
        assertEquals(91.44, LaserMeasurement.parse("100 yd, -100 yd", false, true).get(0).x, 0.01);
        assertEquals(0, LaserMeasurement.parse("100 unitz, -100 unitz", false, false).size());
        assertEquals(100, LaserMeasurement.parse("100m -100m", true, true).get(0).x, 0.01);
        assertEquals(30.48, LaserMeasurement.parse("100ft -100ft", true, true).get(0).x, 0.01);
    }

    @Test
    public void parseErrors() throws ParseException {
        assertEquals(0, LaserMeasurement.parse("100,NaN", true, false).size());
        assertEquals(0, LaserMeasurement.parse("100,Infinity", true, false).size());
        assertEquals(0, LaserMeasurement.parse("100,-Infinity", true, false).size());
        assertEquals(0, LaserMeasurement.parse("100,-100,", true, false).size());
        assertEquals(0, LaserMeasurement.parse("100,-100,333", true, false).size());
        assertEquals(1, LaserMeasurement.parse("100,-100\nXXXX", true, false).size());
        assertEquals(0, LaserMeasurement.parse("100,ZZZ", true, false).size());
        assertEquals(0, LaserMeasurement.parse("100p 200q", true, false).size());
        assertEquals(0, LaserMeasurement.parse("100 200mm", true, false).size());
        assertEquals(0, LaserMeasurement.parse("100 200mm", false, false).size());
    }

    @Test
    public void render() {
        final List<LaserMeasurement> points = testPoints();
        // Metric
        assertEquals("100.0, -100.0\n", LaserMeasurement.render(points, true).toString());
        // Imperial
        assertEquals("328.1, -328.1\n", LaserMeasurement.render(points, false).toString());
    }

    @Test
    public void reorder() {
        final List<LaserMeasurement> points = testPoints();
        points.add(new LaserMeasurement(50, -50));
        assertEquals("100.0, -100.0\n50.0, -50.0\n", LaserMeasurement.render(points, true).toString());

        // Sort quadrant 2
        final List<LaserMeasurement> reordered = LaserMeasurement.reorder(points);
        assertEquals("50.0, -50.0\n100.0, -100.0\n", LaserMeasurement.render(reordered, true).toString());
    }

    @NonNull
    private List<LaserMeasurement> testPoints() {
        final List<LaserMeasurement> points = new ArrayList<>();
        points.add(new LaserMeasurement(100,-100));
        return points;
    }

}
