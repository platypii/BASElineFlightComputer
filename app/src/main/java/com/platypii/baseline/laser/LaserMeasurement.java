package com.platypii.baseline.laser;

import android.support.annotation.NonNull;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LaserMeasurement {
    private static final String TAG = "LaserMeasurement";

    public final double x; // meters
    public final double y; // meters

    LaserMeasurement(double x, double y) {
        this.x = x;
        this.y = y;
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "%.1f, %.1f", x, y);
    }

    public static List<LaserMeasurement> parse(String pointString) {
        final List<LaserMeasurement> points = new ArrayList<>();
        final String[] lines = pointString.split("\n");
        for (String line : lines) {
            final String[] row = line.split(",");
            if (row.length >= 2) {
                try {
                    final double x = Double.parseDouble(row[0]);
                    final double y = Double.parseDouble(row[1]);
                    points.add(new LaserMeasurement(x, y));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Error parsing laser profile", e);
                }
            }
        }
        return points;
    }
}
