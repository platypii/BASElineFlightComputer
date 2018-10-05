package com.platypii.baseline.altimeter;

import java.util.Random;
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
        Filter filter1 = new FilterKalman();
        FilterKalman2 filter2 = new FilterKalman2();
        Random rand = new Random(2010);

        // Add first sample
        filter1.update(10, 0);
        filter1.update(10, 1);
        filter2.init(10, 0);
        filter2.update(10, 1);

        for (int i = 0; i < 10000; i++) {
            double z = rand.nextDouble() * 20;
            filter1.update(z, 1);
            filter2.update(z, 1);
            assertEquals(filter1.x(), filter2.x(), 1);
            assertEquals(filter1.v(), filter2.v(), 1);
        }
    }

}
