package com.platypii.baseline.laser;

import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Exceptions;
import android.support.annotation.NonNull;
import android.util.Log;
import java.text.ParseException;
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
                    Log.w(TAG, "Error parsing laser profile", e);
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

    public static CharSequence render(List<LaserMeasurement> points) {
        final StringBuilder sb = new StringBuilder();
        for (LaserMeasurement point : points) {
            sb.append(String.format(Locale.US, "%.1f, %.1f\n", point.x, point.y));
        }
        return sb;
    }
}
