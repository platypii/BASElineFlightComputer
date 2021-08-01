package com.platypii.baseline.util;

/**
 * Estimates refresh rate from a time series with missing points
 */
public class RefreshRateEstimator {
    private static final float epsilon = 0.05f;

    public float refreshRate = 0; // Hz
    private long lastMillis = -1;

    // How many misses in a row
    private int missedCount = 0;

    public void addSample(long millis) {
        final long delta = millis - lastMillis; // time since last refresh
        // Deltas over 62 seconds are discarded (so we can handle 1 sample/min)
        if (0 <= lastMillis && 0 < delta && delta < 62000L) {
            final float newRefreshRate = 1000f / delta; // Refresh rate based on last 2 samples
            // Special case for missing sample where refreshRate ~= 2 * newRefreshRate
            final boolean missed = 2 * newRefreshRate - epsilon < refreshRate && refreshRate < 2 * newRefreshRate + epsilon;
            if (refreshRate == 0) {
                refreshRate = newRefreshRate;
            } else if (missed && missedCount == 0) {
                // Special case for first missing sample
                missedCount++;
            } else {
                missedCount = 0;
                // Moving average
                refreshRate += (newRefreshRate - refreshRate) * 0.5f;
            }

            // Sanity checks
            if (refreshRate < 0 || Double.isNaN(refreshRate) || Double.isInfinite(refreshRate)) {
                Exceptions.report(new Exception("Invalid refresh rate, delta = " + delta + " refreshRate = " + refreshRate));
                refreshRate = 0;
            }
        }
        lastMillis = millis;
    }

}
