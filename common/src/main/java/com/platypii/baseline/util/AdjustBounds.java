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
        if(Double.isNaN(b.left)) b.left = min.left;
        if(Double.isNaN(b.top)) b.top = min.top;
        if(Double.isNaN(b.right)) b.right = min.right;
        if(Double.isNaN(b.bottom)) b.bottom = min.bottom;
        // If we are still infinite, make it 0..1
        if(Double.isInfinite(b.left)) b.left = 0;
        if(Double.isInfinite(b.top)) b.top = 1;
        if(Double.isInfinite(b.right)) b.right = 1;
        if(Double.isInfinite(b.bottom)) b.bottom = 0;
        // Fit bounds to min/max
        if(b.left > min.left) b.left = min.left;
        if(b.left < max.left) b.left = max.left;
        if(b.top < min.top) b.top = min.top;
        if(b.top > max.top) b.top = max.top;
        if(b.right < min.right) b.right = min.right;
        if(b.right > max.right) b.right = max.right;
        if(b.bottom > min.bottom) b.bottom = min.bottom;
        if(b.bottom < max.bottom) b.bottom = max.bottom;
        if(b.right < b.left) {
            final double tmp = b.right;
            b.right = b.left;
            b.left = tmp;
        }
        if(b.top < b.bottom) {
            final double tmp = b.top;
            b.top = b.bottom;
            b.bottom = tmp;
        }
        if(b.right - b.left < EPSILON) {
            b.left -= EPSILON / 2;
            b.right += EPSILON / 2;
        }
        if(b.top - b.bottom < EPSILON) {
            b.bottom -= EPSILON / 2;
            b.top += EPSILON / 2;
        }
    }

    /**
     * Edits bounds to be the smallest bounds with square aspect ratio containing the old bounds
     */
    public static void squareBounds(@NonNull Bounds b, int width, int height, @NonNull IntBounds padding) {
        final int activeWidth = width - padding.right - padding.left;
        final int activeHeight = height - padding.bottom - padding.top;
        final double aspectCanvas = ((double) activeWidth) / activeHeight;
        final double boundsWidth = b.right - b.left;
        final double boundsHeight = b.top - b.bottom;
        final double aspectBounds = boundsWidth / boundsHeight;
        if(aspectCanvas < aspectBounds) {
            // Anchor b.top
            final double delta = boundsWidth / aspectCanvas - boundsHeight;
            b.set(b.left, b.top, b.right, b.bottom - delta);
        } else {
            // Anchor b.left side
            final double delta = (boundsHeight * aspectCanvas - boundsWidth);
            b.set(b.left, b.top, b.right + delta, b.bottom);
        }
    }

}
