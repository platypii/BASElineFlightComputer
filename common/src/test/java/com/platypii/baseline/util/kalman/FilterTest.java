package com.platypii.baseline.util.kalman;

import java.util.Random;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Sanity checks for kalman filter
 * TODO: Test initialization behavior with noise
 */
public abstract class FilterTest {

    public abstract Filter getFilter();

    @Test
    public void firstSample() {
        Filter filter = getFilter();
        filter.update(10, 0);
        assertEquals(10, filter.x(), .1);
        assertEquals(0, filter.v(), .1);
    }

    @Test
    public void secondSample() {
        Filter filter = getFilter();
        filter.update(10, 0);
        filter.update(20, 1);
        assertEquals(20, filter.x(), .1);
        assertEquals(10, filter.v(), .1);
    }

    @Test
    public void secondSampleInvalid() {
        Filter filter = getFilter();
        filter.update(10, 0);
        filter.update(20, 0);
        filter.update(30, 1);
        assertEquals(30, filter.x(), .1);
        assertEquals(10, filter.v(), .1);
    }

    @Test
    public void constantVelocity() {
        Filter filter = getFilter();
        filter.update(10, 0);
        filter.update(20, 1);
        filter.update(30, 1);
        filter.update(40, 1);
        filter.update(50, 1);
        filter.update(60, 1);
        filter.update(70, 1);
        filter.update(80, 1);
        filter.update(90, 1);
        assertEquals(90, filter.x(), .1);
        assertEquals(10, filter.v(), .1);
    }

    @Test
    public void increaseVelocity() {
        Filter filter = getFilter();
        filter.update(10, 0);
        filter.update(10, 1);
        filter.update(20, 1);
        filter.update(30, 1);
        filter.update(40, 1);
        filter.update(50, 1);
        filter.update(60, 1);
        filter.update(70, 1);
        filter.update(80, 1);
        filter.update(90, 1);
        assertEquals(90, filter.x(), 5.6);
        assertEquals(10, filter.v(), 2.2);
    }

    @Test
    public void sineWave() {
        Filter filter = getFilter();
        filter.update(10, 0);
        filter.update(20, 1);
        for (int i = 30; i < 100000; i += 10) {
            double signal = i + Math.sin(i * 0.1);
            filter.update(signal, 1);
            assertEquals(signal, filter.x(), 1.0);
            assertEquals(10, filter.v(), 1.0);
        }
    }

    @Test
    public void gaussianNoise() {
        Filter filter = getFilter();
        Random rand = new Random(2010);
        filter.update(10, 0);
        filter.update(20, 1);
        for (int i = 30; i < 100000; i += 10) {
            double signal = i + Math.sin(i * 0.1);
            double noise = rand.nextGaussian();
            filter.update(signal + noise, 1);
            assertEquals(signal, filter.x(), 2.8);
            assertEquals(10, filter.v(), 1.0);
        }
    }

    @Test
    public void handleNaN() {
        Filter filter = getFilter();
        filter.update(10, 0);
        filter.update(20, 1);
        filter.update(30, 1);
        filter.update(40, 1);
        filter.update(50, 1);
        filter.update(60, 1);
        filter.update(Double.NaN, 1);
        filter.update(80, 1);
        filter.update(90, 1);
        assertEquals(90, filter.x(), 5);
        assertEquals(10, filter.v(), 2);
    }

    @Test
    public void dupTimestamp() {
        Filter filter = getFilter();
        filter.update(10, 0);
        filter.update(20, 1);
        filter.update(20, 0);
        filter.update(30, 1);
        assertEquals(30, filter.x(), .1);
        assertEquals(10, filter.v(), .1);
    }

    @Test
    public void jumpySensor() {
        Filter filter = getFilter();
        filter.update(0, 0);
        filter.update(1, 1);
        filter.update(0, 1);
        filter.update(1, 1);
        filter.update(0, 1);
        assertEquals(0, filter.x(), .1);
        assertEquals(0, filter.v(), .1);
    }

    @Test
    public void shortLinearTimeGap() {
        Filter filter = getFilter();
        filter.update(10, 0);
        filter.update(20, 1);
        filter.update(30, 1);
        filter.update(40, 1);
        filter.update(140, 10);
        assertEquals(140, filter.x(), .1);
        assertEquals(10, filter.v(), .1);
    }

    @Test
    public void longGroundTimeGap() {
        Filter filter = getFilter();
        Random random = new Random(2010);
        filter.update(0, 0);
        for (int i = 0; i < 1000; i ++) {
            filter.update(random.nextDouble(), 1);
        }

        filter.update(0, 100000);
        assertEquals(0, filter.x(), .1);
        assertEquals(0, filter.v(), .1);

        filter.update(1, 1);
        assertEquals(0, filter.x(), .1);
        assertEquals(0, filter.v(), .1);

        filter.update(1, 1);
        assertEquals(0.12, filter.v(), .01);

        filter.update(0, 1);
        assertEquals(0, filter.v(), .01);
    }

}
