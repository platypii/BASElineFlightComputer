package com.platypii.baseline.util;

import android.support.annotation.NonNull;
import android.util.Log;
import java.util.Locale;

/**
 * Mutable double bounds.
 * Used by PlotView.
 */
public class Bounds {
    private static final String TAG = "Bounds";

    public double left = Double.NaN;
    public double top = Double.NaN;
    public double right = Double.NaN;
    public double bottom = Double.NaN;

    public void set(@NonNull Bounds copy) {
        this.left = copy.left;
        this.top = copy.top;
        this.right = copy.right;
        this.bottom = copy.bottom;
    }

    public void set(double left, double top, double right, double bottom) {
        if(right < left) Log.e(TAG, "Invalid bounds: left should be less than right");
        if(top < bottom) Log.e(TAG, "Invalid bounds: bottom should be less than top");
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public void reset() {
        this.left = Double.NaN;
        this.top = Double.NaN;
        this.right = Double.NaN;
        this.bottom = Double.NaN;
    }

    /**
     * Expands the bounds to include point x,y
     */
    public void expandBounds(double x, double y) {
        if(x < left || Double.isNaN(left)) left = x;
        if(y > top || Double.isNaN(top)) top = y;
        if(x > right || Double.isNaN(right)) right = x;
        if(y < bottom || Double.isNaN(bottom)) bottom = y;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "Bounds(%f,%f,%f,%f)", left, top, right, bottom);
    }
}
