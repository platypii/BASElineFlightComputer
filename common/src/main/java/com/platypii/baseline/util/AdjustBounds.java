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
    public static void clean(@NonNull Bounds b, @NonNull Bounds min, @NonNull Bounds max) {
        // If bounds are NaN, then use smallest legal viewing window
        if (Double.isNaN(b.x.min)) b.x.min = min.x.min;
        if (Double.isNaN(b.y.max)) b.y.max = min.y.max;
        if (Double.isNaN(b.x.max)) b.x.max = min.x.max;
        if (Double.isNaN(b.y.min)) b.y.min = min.y.min;
        // If we are still infinite, make it 0..1
        if (Double.isInfinite(b.x.min)) b.x.min = 0;
        if (Double.isInfinite(b.y.max)) b.y.max = 1;
        if (Double.isInfinite(b.x.max)) b.x.max = 1;
        if (Double.isInfinite(b.y.min)) b.y.min = 0;
        // Fit bounds to min/max
        if (b.x.min > min.x.min) b.x.min = min.x.min;
        if (b.x.min < max.x.min) b.x.min = max.x.min;
        if (b.y.max < min.y.max) b.y.max = min.y.max;
        if (b.y.max > max.y.max) b.y.max = max.y.max;
        if (b.x.max < min.x.max) b.x.max = min.x.max;
        if (b.x.max > max.x.max) b.x.max = max.x.max;
        if (b.y.min > min.y.min) b.y.min = min.y.min;
        if (b.y.min < max.y.min) b.y.min = max.y.min;
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
