package com.platypii.baseline.util;

import androidx.annotation.NonNull;
import android.util.Log;
import java.util.Locale;

/**
 * Mutable finite double bounds.
 * Used by PlotView.
 */
public class Bounds {
    private static final String TAG = "Bounds";

    public final Range x = new Range();
    public final Range y = new Range();

//    public double left = Double.NaN;
//    public double top = Double.NaN;
//    public double right = Double.NaN;
//    public double bottom = Double.NaN;

    public void set(@NonNull Bounds copy) {
        this.x.min = copy.x.min;
        this.x.max = copy.x.max;
        this.y.min = copy.y.min;
        this.y.max = copy.y.max;
    }

    public void set(double left, double top, double right, double bottom) {
        if (right < left) Log.e(TAG, "Invalid bounds: left should be less than right");
        if (top < bottom) Log.e(TAG, "Invalid bounds: bottom should be less than top");
        this.x.min = left;
        this.x.max = right;
        this.y.min = bottom;
        this.y.max = top;
    }

    public void reset() {
        this.x.min = Double.NaN;
        this.x.max = Double.NaN;
        this.y.min = Double.NaN;
        this.y.max = Double.NaN;
    }

    /**
     * Expands the bounds to include point x,y
     */
    public void expandBounds(double x0, double y0) {
        // Make sure it is finite, but NaN will be handled by Range
        if (!Double.isInfinite(x0)) {
            x.expand(x0);
        }
        if (!Double.isInfinite(y0)) {
            y.expand(y0);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return String.format(Locale.US, "Bounds(x=%f,%f y=%f,%f)", x.min, x.max, y.min, y.max);
    }
}
