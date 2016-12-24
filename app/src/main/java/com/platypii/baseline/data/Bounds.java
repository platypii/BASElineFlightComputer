package com.platypii.baseline.data;

/**
 * Mutable double bounds.
 * Used by PlotView.
 */
public class Bounds {

    public double left;
    public double top;
    public double right;
    public double bottom;
    private static final double EPSILON = 0.001;

    public Bounds() {
        this.left = Double.NaN;
        this.top = Double.NaN;
        this.right = Double.NaN;
        this.bottom = Double.NaN;
    }

    public Bounds(double left, double top, double right, double bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public void set(Bounds copy) {
        this.left = copy.left;
        this.top = copy.top;
        this.right = copy.right;
        this.bottom = copy.bottom;
    }

    public void set(double left, double top, double right, double bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    /**
     * Expands the bounds to include point x,y
     */
    public void expandBounds(double x, double y) {
        if(x < left) left = x;
        if(y > top) top = y;
        if(x > right) right = x;
        if(y < bottom) bottom = y;
    }

    /**
     * Clean the bounds (satisfy min/max, no infinities, and width/height span of at least epsilon)
     */
    public void clean(Bounds min, Bounds max) {
        // If bounds are NaN, then use smallest legal viewing window
        if(Double.isNaN(left)) left = max.left;
        if(Double.isNaN(top)) top = max.top;
        if(Double.isNaN(right)) right = min.right;
        if(Double.isNaN(bottom)) bottom = max.bottom;
        // If we are still infinite, make it 0..1
        if(Double.isInfinite(left)) left = 0;
        if(Double.isInfinite(top)) top = 1;
        if(Double.isInfinite(right)) right = 1;
        if(Double.isInfinite(bottom)) bottom = 0;
        // Fit bounds to min/max
        if(left < min.left) left = min.left;
        if(left > max.left) left = max.left;
        if(top < min.top) top = min.top;
        if(top > max.top) top = max.top;
        if(right < min.right) right = min.right;
        if(right > max.right) right = max.right;
        if(bottom < min.bottom) bottom = min.bottom;
        if(bottom > max.bottom) bottom = max.bottom;
        if(right < left) {
            final double tmp = right;
            right = left;
            left = tmp;
        }
        if(top < bottom) {
            final double tmp = top;
            top = bottom;
            bottom = tmp;
        }
        if(right - left < EPSILON) {
            left -= EPSILON / 2;
            right += EPSILON / 2;
        }
        if(top - bottom < EPSILON) {
            bottom -= EPSILON / 2;
            top += EPSILON / 2;
        }
    }

    /**
     * Edits bounds to be the smallest bounds with square aspect ratio containing the old bounds
     */
    public void squareBounds(int width, int height, IntBounds padding) {
        final int activeWidth = width - padding.right - padding.left;
        final int activeHeight = height - padding.bottom - padding.top;
        final double aspectCanvas = ((double) activeWidth) / activeHeight;
        final double boundsWidth = right - left;
        final double boundsHeight = top - bottom;
        final double aspectBounds = boundsWidth / boundsHeight;
        if(aspectCanvas < aspectBounds) {
            // Anchor top
            final double delta = boundsWidth / aspectCanvas - boundsHeight;
            set(left, top, right, bottom - delta);
        } else {
            // Anchor left side
            final double delta = (boundsHeight * aspectCanvas - boundsWidth);
            set(left, top, right + delta, bottom);
        }
    }
}
