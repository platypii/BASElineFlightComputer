package com.platypii.baseline.util.filters;

import androidx.annotation.NonNull;

/**
 * Sanity checks for kalman filter
 */
public class KalmanTest extends FilterTest {

    @NonNull
    @Override
    public Filter getFilter() {
        return new FilterKalman();
    }

}
