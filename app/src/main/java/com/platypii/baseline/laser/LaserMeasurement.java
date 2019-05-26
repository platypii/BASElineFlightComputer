package com.platypii.baseline.laser;

import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.util.Range;
import android.util.Log;
import androidx.annotation.NonNull;
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
            Log.w(TAG, "Invalid horizontal distance " + x + " " + y);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "%.1f, %.1f", x, y);
    }

    @NonNull
    public static List<LaserMeasurement> parse(@NonNull String pointString, boolean metric, boolean strict) throws ParseException {
        final List<LaserMeasurement> points = new ArrayList<>();
        final String[] lines = pointString.split("\n");
        final double units = metric ? 1 : Convert.FT;
        for (int i = 0; i < lines.length; i++) {
            final String line = lines[i];
            final String[] row = line.split(",");
            if (row.length == 2) {
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

    @NonNull
    public static List<LaserMeasurement> parseSafe(@NonNull String pointString, boolean metric) {
        try {
            return parse(pointString, metric, false);
        } catch (ParseException e) {
            // Parse exception should never actually be thrown when strict = false
            Exceptions.report(e);
            return new ArrayList<>();
        }
    }

    @NonNull
    public static CharSequence render(List<LaserMeasurement> points, boolean metric) {
        final double units = metric ? 1 : 3.28084;
        final StringBuilder sb = new StringBuilder();
        for (LaserMeasurement point : points) {
            sb.append(String.format(Locale.US, "%.1f, %.1f\n", point.x * units, point.y * units));
        }
        return sb;
    }

    /**
     * There are three laser input formats:
     * Quadrant 2: 20,-100
     * Quadrant 1: 20,100 (laser from bottom)
     * Quadrant 4: -100,20 (reversed y,x)
     */
    @NonNull
    public static List<LaserMeasurement> reorder(List<LaserMeasurement> points) {
        // Find height and width range
        final Range xRange = new Range();
        final Range yRange = new Range();
        for (LaserMeasurement point : points) {
            xRange.expand(point.x);
            yRange.expand(point.y);
        }
        if (xRange.min < 0 && xRange.max <= 0 && yRange.min >= 0) {
            // Quadrant 4: Assume coordinates are reversed y,x
            final List<LaserMeasurement> swapped = new ArrayList<>();
            for (LaserMeasurement point : points) {
                //noinspection SuspiciousNameCombination
                swapped.add(new LaserMeasurement(point.y, point.x));
            }
            // Sort by horiz
            Collections.sort(swapped, (l1, l2) -> Double.compare(l1.x, l2.x));
            return swapped;
        } else if (yRange.min >= 0 && yRange.max > 0) {
            // Quadrant 1: Assume lasering from bottom
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
            // Quadrant 2: default x,y
            final List<LaserMeasurement> sorted = new ArrayList<>(points);
            // Sort by horiz
            Collections.sort(sorted, (l1, l2) -> Double.compare(l1.x, l2.x));
            return sorted;
        }
    }

}
