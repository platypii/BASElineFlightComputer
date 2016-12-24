package com.platypii.baseline.data;

/**
 * Mutable integer bounds.
 * Used by PlotView to represent padding.
 */
public class IntBounds {

    public int left;
    public int top;
    public int right;
    public int bottom;

    public IntBounds(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

}
