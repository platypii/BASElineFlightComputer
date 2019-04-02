package com.platypii.baseline.laser;

import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.util.Range;
import android.support.annotation.NonNull;
import android.util.Log;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class LaserMeasurement {
    private static final String TAG = "LaserMeasurement";

    public final double x; // meters
    public final double y; // meters

    LaserMeasurement(double x, double y) {
        this.x = x;
        this.y = y;
        if (x < 0) {
            throw new IllegalArgumentException("Invalid horizontal distance " + x + " " + y);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "%.1f, %.1f", x, y);
    }

    public static List<LaserMeasurement> parse(String pointString, boolean metric, boolean strict) throws ParseException {
        final List<LaserMeasurement> points = new ArrayList<>();
        final String[] lines = pointString.split("\n");
        final double units = metric ? 1 : Convert.FT;
        for (int i = 0; i < lines.length; i++) {
            final String line = lines[i];
            final String[] row = line.split(",");
            if (row.length >= 2) {
                try {
                    final double x = Double.parseDouble(row[0]) * units;
                    final double y = Double.parseDouble(row[1]) * units;
                    points.add(new LaserMeasurement(x, y));
                } catch (NumberFormatException e) {
                    Log.w(TAG, "Error parsing laser profile " + e);
                    if (strict) {
                        throw new ParseException("Invalid measurement", i + 1);
                    }
                }
            } else {
                if (strict) {
                    throw new ParseException("Invalid measurement", i + 1);
                }
            }
        }
        return points;
    }

    public static List<LaserMeasurement> parseSafe(String pointString, boolean metric) {
        try {
            return parse(pointString, metric, false);
        } catch (ParseException e) {
            // Parse exception should never actually be thrown when strict = false
            Exceptions.report(e);
            return new ArrayList<>();
        }
    }

    public static List<LaserMeasurement> reorder(List<LaserMeasurement> points) {
        // Find height and width range
        final Range xRange = new Range();
        final Range yRange = new Range();
        for (LaserMeasurement point : points) {
            xRange.expand(point.x);
            yRange.expand(point.y);
        }
        // Check if reversible
        if (yRange.min >= 0) {
            final List<LaserMeasurement> reversed = new ArrayList<>();
            for (LaserMeasurement point : points) {
                reversed.add(new LaserMeasurement(xRange.max - point.x, point.y - yRange.max));
            }
            // Add reversed 0,0
            reversed.add(new LaserMeasurement(xRange.max, - yRange.max));
            // Sort by horiz
            Collections.sort(reversed, (l1, l2) -> Double.compare(l1.x, l2.x));
            return reversed;
        } else {
            return points;
        }
    }

    public static CharSequence render(List<LaserMeasurement> points) {
        final StringBuilder sb = new StringBuilder();
        for (LaserMeasurement point : points) {
            sb.append(String.format(Locale.US, "%.1f, %.1f\n", point.x, point.y));
        }
        return sb;
    }
}
