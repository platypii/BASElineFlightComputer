package com.platypii.baseline.util;

import android.support.annotation.NonNull;

/**
 * Adjust view bounds to satisfy constraints
 */
public class AdjustBounds {
    private static final double EPSILON = 0.001;

    /**
     * Clean the bounds (satisfy min/max, no infinities, and width/height span of at least epsilon)
     */
    public static void clean(@NonNull Bounds b, @NonNull Bounds inner, @NonNull Bounds outer) {
        // Extra checks that shouldn't be needed at runtime
//        if (inner.x.min < outer.x.min) Exceptions.report(new IllegalArgumentException("inner x min exceeds outer bound"));
//        if (inner.x.max > outer.x.max) Exceptions.report(new IllegalArgumentException("inner x max exceeds outer bound"));
//        if (inner.y.min < outer.y.min) Exceptions.report(new IllegalArgumentException("inner y min exceeds outer bound"));
//        if (inner.y.max > outer.y.max) Exceptions.report(new IllegalArgumentException("inner y max exceeds outer bound"));
        // If bounds are NaN, then use smallest legal viewing window
        if (Double.isNaN(b.x.min)) b.x.min = inner.x.min;
        if (Double.isNaN(b.y.max)) b.y.max = inner.y.max;
        if (Double.isNaN(b.x.max)) b.x.max = inner.x.max;
        if (Double.isNaN(b.y.min)) b.y.min = inner.y.min;
        // If we are still infinite, make it 0..1
        if (Double.isInfinite(b.x.min)) b.x.min = 0;
        if (Double.isInfinite(b.y.max)) b.y.max = 1;
        if (Double.isInfinite(b.x.max)) b.x.max = 1;
        if (Double.isInfinite(b.y.min)) b.y.min = 0;
        // Fit bounds to min/max
        if (b.x.min > inner.x.min) b.x.min = inner.x.min;
        if (b.x.min < outer.x.min) b.x.min = outer.x.min;
        if (b.y.max < inner.y.max) b.y.max = inner.y.max;
        if (b.y.max > outer.y.max) b.y.max = outer.y.max;
        if (b.x.max < inner.x.max) b.x.max = inner.x.max;
        if (b.x.max > outer.x.max) b.x.max = outer.x.max;
        if (b.y.min > inner.y.min) b.y.min = inner.y.min;
        if (b.y.min < outer.y.min) b.y.min = outer.y.min;
        if (b.x.max < b.x.min) {
            final double tmp = b.x.max;
            b.x.max = b.x.min;
            b.x.min = tmp;
        }
        if (b.y.max < b.y.min) {
            final double tmp = b.y.max;
            b.y.max = b.y.min;
            b.y.min = tmp;
        }
        if (b.x.max - b.x.min < EPSILON) {
            b.x.min -= EPSILON / 2;
            b.x.max += EPSILON / 2;
        }
        if (b.y.max - b.y.min < EPSILON) {
            b.y.min -= EPSILON / 2;
            b.y.max += EPSILON / 2;
        }
    }

    /**
     * Edits bounds to be the smallest bounds with square aspect ratio containing the old bounds
     */
    public static void squareBounds(@NonNull Bounds b, int width, int height, @NonNull IntBounds padding) {
        final int activeWidth = width - padding.right - padding.left;
        final int activeHeight = height - padding.bottom - padding.top;
        final double aspectCanvas = ((double) activeWidth) / activeHeight;
        final double boundsWidth = b.x.max - b.x.min;
        final double boundsHeight = b.y.max - b.y.min;
        final double aspectBounds = boundsWidth / boundsHeight;
        if (aspectCanvas < aspectBounds) {
            // Anchor b.y.max
            final double delta = boundsWidth / aspectCanvas - boundsHeight;
            b.set(b.x.min, b.y.max, b.x.max, b.y.min - delta);
        } else {
            // Anchor b.x.min side
            final double delta = (boundsHeight * aspectCanvas - boundsWidth);
            b.set(b.x.min, b.y.max, b.x.max + delta, b.y.min);
        }
    }

}
