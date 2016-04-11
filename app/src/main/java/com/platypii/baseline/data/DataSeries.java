package com.platypii.baseline.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class DataSeries implements Iterable<DataSeries.Point> {

    private final ArrayList<Point> points = new ArrayList<>();
    private int n = 0;

    public class Point {
        public double x;
        public double y;
        public Point(double x, double y) {
            this.x = x;
            this.y = y;
        }
        @Override
        public String toString() {
            return String.format("Point(%.1f,%.1f)", x, y);
        }
    }

    public void addPoint(double x, double y) {
        assert !Double.isNaN(x) && !Double.isInfinite(x);
        assert !Double.isNaN(y) && !Double.isInfinite(y);
        if(n < points.size()) {
            Point point = points.get(n);
            point.x = x;
            point.y = y;
        } else {
            points.add(new Point(x,y));
        }
        n++;
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

    public Iterator<Point> iterator() {
        return new Iterator<Point>() {
            private int i = 0;
            public boolean hasNext() {
                return i < n;
            }
            public Point next() {
                if(i < n)
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
