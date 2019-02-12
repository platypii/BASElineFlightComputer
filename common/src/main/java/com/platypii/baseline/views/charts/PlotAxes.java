package com.platypii.baseline.views.charts;

import com.platypii.baseline.util.Bounds;
import com.platypii.baseline.util.IntBounds;
import com.platypii.baseline.util.Numbers;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Drawing tools for plot axes
 */
class PlotAxes {
    private static final String TAG = "PlotAxes";

    // Draw axes based on axis 0 only
    private static final int AXIS_DEFAULT = 0;

    private final double EPSILON = 0.001;

    @NonNull
    private final Plot plot;

    PlotAxes(@NonNull Plot plot) {
        this.plot = plot;
    }

    /**
     * Called when rendering the plot to draw the plot axis lines and labels.
     */
    void drawGridlines() {
        plot.paint.setStrokeWidth(0);
        plot.paint.setTextSize(20);
        final Bounds realBounds = getRealBounds();
        drawXlines(realBounds);
        drawYlines(realBounds);
    }

    /**
     * Draw the grid lines for the current view between the given screen-space coordinates
     * Default behavior is to draw grid lines for the nearest order of magnitude along the axis
     */
    private static final int MAX_LINES = 80;
    private void drawXlines(@NonNull Bounds realBounds) {
        final int magnitude_x = (int) Math.log10((realBounds.x.max - realBounds.x.min) / plot.options.axis.x.major_units);
        final double step_x = plot.options.axis.x.major_units * Numbers.pow(10, magnitude_x);
        final double start_x = Math.floor(realBounds.x.min / step_x) * step_x;
        final double end_x = Math.ceil(realBounds.x.max / step_x) * step_x;
        final int steps_x = (int) Math.ceil((end_x - start_x) / step_x);
        if (!(start_x < end_x && 0 < step_x)) {
            Log.e(TAG, "Invalid plot X bounds " + start_x + " " + end_x + " " + step_x);
        }
        for (int n = 0; n < steps_x; n++) {
            final double x = start_x + n * step_x;
            if (Math.abs(x) < EPSILON) {
                drawXline(x, plot.options.axis_color, plot.options.axis.x.format(x));
            } else {
                drawXline(x, plot.options.grid_color, plot.options.axis.x.format(x));
            }

            if (n > MAX_LINES) {
                Log.e(TAG, "Too many X grid lines!");
                break;
            }
        }
    }
    private void drawYlines(@NonNull Bounds realBounds) {
        final int magnitude_y = (int) Math.log10((realBounds.y.max - realBounds.y.min) / plot.options.axis.y.major_units);
        final double step_y = plot.options.axis.y.major_units * Numbers.pow(10, magnitude_y); // grid spacing in plot-space
        final double start_y = Math.floor(realBounds.y.min / step_y) * step_y;
        final double end_y = Math.ceil(realBounds.y.max / step_y) * step_y;
        final int steps_y = (int) Math.ceil((end_y - start_y) / step_y);
        // Log.i(TAG, "bounds = " + bounds + ", realBounds = " + realBounds);
        // Log.i(TAG, "start_y = " + start_y + ", end_y = " + end_y + ", magnitude_y = " + magnitude_y + ", step_y = " + step_y);
        if (!(start_y <= end_y && 0 < step_y)) {
            Log.e(TAG, "Invalid plot Y bounds " + start_y + " " + end_y + " " + step_y);
        }
        for (int n = 0; n < steps_y; n++) {
            final double y = start_y + n * step_y;
            if (Math.abs(y) < EPSILON) {
                drawYline(y, plot.options.axis_color, plot.options.axis.y.format(y));
            } else {
                drawYline(y, plot.options.grid_color, plot.options.axis.y.format(y));
            }

            if (n > MAX_LINES) {
                Log.e(TAG, "Too many Y grid lines!");
                break;
            }
        }
    }

    /**
     * Draws an X grid line (vertical)
     */
    private void drawXline(double x, int color, @Nullable String label) {
        // Screen coordinate
        final int sx = (int) plot.getX(x);
        plot.paint.setColor(color);
        plot.canvas.drawLine(sx, 0, sx, plot.height, plot.paint);
        if (label != null) {
            plot.paint.setColor(plot.options.grid_color);
            plot.paint.setTextAlign(Paint.Align.LEFT);
            plot.canvas.drawText(label, sx + 2 * plot.options.density, 10 * plot.options.density, plot.paint);
        }
    }
    /**
     * Draws a Y grid line (horizontal)
     */
    private void drawYline(double y, int color, @Nullable String label) {
        final int sy = (int) plot.getY(y);
        plot.paint.setColor(color);
        plot.canvas.drawLine(0, sy, plot.width, sy, plot.paint);
        if (label != null) {
            plot.paint.setColor(plot.options.grid_color);
            // Left align
            plot.canvas.drawText(label, 2 * plot.options.density, sy - 2 * plot.options.density, plot.paint);
            // Right align
            // text.setTextAlign(Paint.Align.RIGHT);
            // canvas.drawText(label, right - 2 * density, sy - 2 * density, text);
        }
    }

//    public void drawXtick(double x, String label) {
//        final int sx = (int) plot.getX(x);
//        final int sy = (int) plot.getY(0);
//        plot.canvas.drawLine(sx, sy - 4 * plot.options.density, sx, sy + 4 * plot.options.density, paint);
//        text.setTextAlign(Paint.Align.CENTER);
//        plot.canvas.drawText(label, sx, sy + 19 * plot.options.density, text);
//    }
//    public void drawYtick(double y, String label) {
//        final int sx = (int) plot.getX(0);
//        final int sy = (int) plot.getY(y);
//        plot.canvas.drawLine(sx - 4 * plot.options.density, sy, sx + 4 * plot.options.density, sy, paint);
//        text.setTextAlign(Paint.Align.LEFT);
//        plot.canvas.drawText(label, sx + 7 * plot.options.density, sy + 6 * plot.options.density, text);
//    }

    // Returns the bounds in plot-space, including padding
    private final Bounds realBounds = new Bounds();
    @NonNull
    private Bounds getRealBounds() {
        final IntBounds padding = plot.options.padding;
        final Bounds bounds = plot.bounds[AXIS_DEFAULT];
        final double ppm_x = (plot.width - padding.right - padding.left) / (bounds.x.max - bounds.x.min); // pixels per meter
        final double rLeft = bounds.x.min - padding.left / ppm_x; // min x-coordinate in plot-space
        final double rRight = bounds.x.min + (plot.width - padding.left) / ppm_x; // max x-coordinate in plot-space
        final double ppm_y = (plot.height - padding.bottom - padding.top) / (bounds.y.max - bounds.y.min); // pixels per meter
        final double rBottom = bounds.y.min - padding.bottom / ppm_y; // min y-coordinate in plot-space
        final double rTop = bounds.y.min + (plot.height - padding.bottom) / ppm_y; // max y-coordinate in plot-space
        realBounds.set(rLeft, rTop, rRight, rBottom);
        return realBounds;
    }

}
