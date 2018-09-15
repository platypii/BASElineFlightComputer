package com.platypii.baseline.altimeter;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Sanity checks for kalman filter
 */
public class KalmanTest2 extends FilterTest {

    @Override
    public Filter getFilter() {
        return new FilterKalman2();
    }

    @Test
    public void sameSame() {
        Filter filter1 = new FilterKalman2();
        Filter filter2 = new FilterKalman2();

        // Add first sample
        filter1.init(10, 0);
        filter2.init(10, 0);

        for (int i = 0; i < 1000; i++) {
            double z = Math.random() * 20;
            filter1.update(z, 1);
            filter2.update(z, 1);
            assertEquals(filter1.x(), filter2.x(), .001);
            assertEquals(filter1.v(), filter2.v(), .001);
        }
    }


}
