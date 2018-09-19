package com.platypii.baseline.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Ensure that we are mathing correctly
 */
public class RefreshRateEstimatorTest {

    @Test
    public void init1hz() {
        RefreshRateEstimator estimator = new RefreshRateEstimator();
        estimator.addSample(0);
        estimator.addSample(1000);
        assertEquals(1.0, estimator.refreshRate, 0.01);
        estimator.addSample(2000);
        estimator.addSample(3000);
        assertEquals(1.0, estimator.refreshRate, 0.01);
    }

    @Test
    public void init5hz() {
        RefreshRateEstimator estimator = new RefreshRateEstimator();
        estimator.addSample(0);
        estimator.addSample(200);
        assertEquals(5.0, estimator.refreshRate, 0.01);
        estimator.addSample(400);
        estimator.addSample(600);
        assertEquals(5.0, estimator.refreshRate, 0.01);
    }

    @Test
    public void initDuplicate() {
        RefreshRateEstimator estimator = new RefreshRateEstimator();
        estimator.addSample(200);
        estimator.addSample(200);
        estimator.addSample(400);
        assertEquals(5.0, estimator.refreshRate, 0.01);
        estimator.addSample(600);
        estimator.addSample(800);
        assertEquals(5.0, estimator.refreshRate, 0.01);
    }

    @Test
    public void missingSample() {
        RefreshRateEstimator estimator = new RefreshRateEstimator();
        estimator.addSample(0);
        estimator.addSample(200);
        estimator.addSample(400);
        assertEquals(5.0, estimator.refreshRate, 0.01);
        // estimator.addSample(600);
        estimator.addSample(800);
        assertEquals(5.0, estimator.refreshRate, 0.01);
    }

    @Test
    public void halfRefreshRate() {
        // Drop from 2hz to 1hz
        RefreshRateEstimator estimator = new RefreshRateEstimator();
        estimator.addSample(0);
        estimator.addSample(500);
        estimator.addSample(1000);
        assertEquals(2.0, estimator.refreshRate, 0.01);
        estimator.addSample(2000);
        assertEquals(2.0, estimator.refreshRate, 0.01); // assumes missing sample
        estimator.addSample(3000);
        assertEquals(1.0, estimator.refreshRate, 0.5); // starts adjusting
        estimator.addSample(4000);
        assertEquals(1.0, estimator.refreshRate, 0.25);
        estimator.addSample(5000);
        estimator.addSample(6000);
        assertEquals(1.0, estimator.refreshRate, 0.1);
        estimator.addSample(7000);
        estimator.addSample(8000);
        estimator.addSample(9000);
        assertEquals(1.0, estimator.refreshRate, 0.01); // close enough
    }

}
