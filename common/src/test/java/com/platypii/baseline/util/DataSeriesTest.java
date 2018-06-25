package com.platypii.baseline.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are mathing correctly
 */
public class DataSeriesTest {

    @Test
    public void add() {
        DataSeries series = new DataSeries();

        assertEquals(0, series.size());
        series.addPoint(1,2);
        assertEquals(1, series.size());
    }

    @Test
    public void reset() {
        DataSeries series = new DataSeries();

        assertEquals(0, series.size());
        series.addPoint(1,2);
        assertEquals(1, series.size());
        series.reset();
        assertEquals(0, series.size());
    }

    @Test
    public void reuse() {
        DataSeries series = new DataSeries();

        assertEquals(0, series.size());
        series.addPoint(1,2);
        assertEquals(1, series.size());
        series.reset();
        assertEquals(0, series.size());
        series.addPoint(2,3);
        assertEquals(1, series.size());
    }

    @Test
    public void iterator() {
        DataSeries series = new DataSeries();
        series.addPoint(1,2);
        series.addPoint(2,3);

        int count = 0;
        for (DataSeries.Point point : series) {
            count++;
        }
        assertEquals(2, count);
    }

}
