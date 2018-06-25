package com.platypii.baseline.util;

import android.support.annotation.NonNull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.NoSuchElementException;

/**
 * Represent a series of data points to be plotted.
 */
public class DataSeries implements Iterable<DataSeries.Point> {

    private final ArrayList<Point> points = new ArrayList<>();
    private Point lastPoint;
    private int n = 0;

    public static class Point {
        public double x;
        public double y;
        Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
        boolean isNaN() {
            return Double.isNaN(x) || Double.isInfinite(x) || Double.isNaN(y) || Double.isInfinite(y);
        }
        @Override
        public String toString() {
            return String.format(Locale.US, "Point(%.1f,%.1f)", x, y);
        }
    }

    public void addPoint(double x, double y) {
        final boolean isNaN = Double.isNaN(x) || Double.isInfinite(x) || Double.isNaN(y) || Double.isInfinite(y);
        // Skip double NaN
        if (lastPoint == null || !lastPoint.isNaN() || !isNaN) {
            if (n < points.size()) {
                lastPoint = points.get(n);
                lastPoint.x = x;
                lastPoint.y = y;
            } else {
                lastPoint = new Point(x, y);
                points.add(lastPoint);
            }
            n++;
        }
    }

    public int size() {
        return n;
    }

    /**
     * Resets the data series, but does not remove old structures to save time/space
     */
    public void reset() {
        n = 0;
    }

    @NonNull
    public Iterator<Point> iterator() {
        return new Iterator<Point>() {
            private int i = 0;
            public boolean hasNext() {
                return i < n;
            }
            public Point next() {
                if (i < n)
                    return points.get(i++);
                else
                    throw new NoSuchElementException();
            }
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

}
