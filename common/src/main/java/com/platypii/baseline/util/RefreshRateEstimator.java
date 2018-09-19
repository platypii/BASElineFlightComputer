package com.platypii.baseline.util;

public class RefreshRateEstimator {

    public float refreshRate = 0;
    private long lastMillis = -1;

    public void addSample(long millis) {
        final long delta = millis - lastMillis; // time since last refresh
        // Deltas over 62 seconds are discarded (so we can handle 1 sample/min)
        if (0 <= lastMillis && 0 < delta && delta < 62000L) {
            final float newRefreshRate = 1000f / delta; // Refresh rate based on last 2 samples
            if (refreshRate == 0) {
                refreshRate = newRefreshRate;
            } else {
                refreshRate += (newRefreshRate - refreshRate) * 0.5f; // Moving average
            }

            // Sanity checks
            if (Double.isNaN(refreshRate) || Double.isInfinite(refreshRate)) {
                Exceptions.report(new Exception("Invalid refresh rate, delta = " + delta + " refreshRate = " + refreshRate));
                refreshRate = 0;
            }
        }
        lastMillis = millis;
    }

}
