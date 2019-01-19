package com.platypii.baseline.laser;

import android.support.annotation.NonNull;
import java.util.Locale;

class LaserMeasurement {
    final double pitch; // degrees
    final double total; // meters
    final double vert; // meters
    final double horiz; // meters

    LaserMeasurement(double pitch, double total, double vert, double horiz) {
        this.pitch = pitch;
        this.total = total;
        this.vert = vert;
        this.horiz = horiz;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "pitch=%.1f total=%.1f vert=%.1f horiz=%.1f", pitch, total, vert, horiz);
    }
}
