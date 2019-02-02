package com.platypii.baseline.laser;

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
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "%.1f, %.1f", x, y);
    }

    public static List<LaserMeasurement> parse(String pointString, boolean strict) throws ParseException {
        final List<LaserMeasurement> points = new ArrayList<>();
        final String[] lines = pointString.split("\n");
        for (int i = 0; i < lines.length; i++) {
            final String line = lines[i];
            final String[] row = line.split(",");
            if (row.length >= 2) {
                try {
                    final double x = Double.parseDouble(row[0]);
                    final double y = Double.parseDouble(row[1]);
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

    public static List<LaserMeasurement> parseSafe(String pointString) {
        try {
            return parse(pointString, false);
        } catch (ParseException e) {
            // Parse exception should never actually be thrown when strict = false
            Exceptions.report(e);
            return new ArrayList<>();
        }
    }
}
