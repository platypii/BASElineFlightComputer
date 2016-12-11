package com.platypii.baseline.alti;

import java.util.Locale;

/**
 * A class to track mean and variance
 */
class Stat {

    private long n = 0;
    // private double total = 0.0;
    private double mean = 0.0;
    private double M2 = 0.0;

    void addSample(double x) {
        // Online mean and variance (thanks Knuth)
        n++;
        // total += x;
        final double delta = x - mean;
        mean = mean + delta / n;
        M2 = M2 + delta * (x - mean);
    }

    double var() {
        // Sample Variance
        return M2 / n;

        // Population Variance
        // return M2 / (n - 1);
    }

    public String toString() {
        return String.format(Locale.US, "%.3f ± %.3f", mean, var());
    }

}
