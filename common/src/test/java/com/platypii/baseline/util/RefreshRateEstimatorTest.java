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

}
