package com.platypii.baseline.altimeter;

/**
 * Sanity checks for kalman filter
 */
public class KalmanTest extends FilterTest {

    @Override
    public Filter getFilter() {
        return new FilterKalman();
    }

}
