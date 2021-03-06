package com.platypii.baseline.views.charts;

import com.platypii.baseline.util.Bounds;
import com.platypii.baseline.util.DataSeries;
import com.platypii.baseline.util.IntBounds;
import com.platypii.baseline.views.charts.layers.ChartLayer;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Wraps a canvas, but operations are in plot-space x,y
 */
public class Plot {

    // Drawing options
    @NonNull
    public final PlotOptions options;
    final PlotAxes axes = new PlotAxes(this);

    // Drawing surface
    public Canvas canvas;
    public int width;
    public int height;

    // Bounds are indexed per-axis
    // Data bounds get expanded as we see points
    private Bounds[] dataBounds;
    // Plot bounds get updated once at the beginning of drawPlot
    Bounds[] bounds;

    // Chart layers
    public final List<ChartLayer> layers = new ArrayList<>();


    // Avoid creating new objects unnecessarily
    private final Path path = new Path();
    public final Paint paint = new Paint();
    public final Paint text = new Paint();

    Plot(@NonNull PlotOptions options) {
        this.options = options;
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setStrokeJoin(Paint.Join.ROUND);
        text.setAntiAlias(true);
        text.setTextSize(options.font_size);
        text.setColor(0xffcccccc);
    }

    /**
     * Always call setCanvas before drawing or bad things will happen
     */
    void setCanvas(@NonNull Canvas canvas) {
        if (this.canvas != canvas) {
            this.canvas = canvas;
            // Cache the width and height since canvas.getWidth is slowwww
            width = canvas.getWidth();
            height = canvas.getHeight();
        }
    }

    /**
     * Update bounds from dataBounds
     */
    void updateBounds(@NonNull BasePlot basePlot) {
        for (int i = 0; i < bounds.length; i++) {
            // Get plot-space bounds from subclass, and copy to bounds
            final Bounds plotBounds = basePlot.getBounds(dataBounds[i], i);
            bounds[i].set(plotBounds);
            // Reset data bounds
            dataBounds[i].reset();
        }
    }

    /**
     * Initialize bounds for a given number of axes
     */
    void initBounds(int axes) {
        bounds = new Bounds[axes];
        dataBounds = new Bounds[axes];
        for (int i = 0; i < axes; i++) {
            bounds[i] = new Bounds();
            dataBounds[i] = new Bounds();
        }
    }

    /**
     * Draws a point (input given in plot-space)
     *
     * @param strokeWidth the stroke width of the path
     */
    public void drawPoint(int axis, double x, double y, float strokeWidth) {
        dataBounds[axis].expandBounds(x, y);
        // Screen coordinates
        float sx = getX(axis, x);
        float sy = getY(axis, y);
        // canvas.drawPoint(x, y, paint);
        canvas.drawCircle(sx, sy, strokeWidth * options.density, paint);
    }

//    /**
//     * Draws a series of points as dots
//     * @param series the data series to draw
//     * @param strokeWidth the width of the path
//     */
//    void drawPoints(@NonNull DataSeries series, float strokeWidth) {
//        paint.setStyle(Paint.Style.FILL);
//        for (DataSeries.Point point : series) {
//            dataBounds.expandBounds(point.x, point.y);
//            canvas.drawCircle(getX(point.x), getY(point.y), strokeWidth * options.density, paint);
//        }
//    }

    /**
     * Draws a series of points (input given in plot-space)
     *
     * @param series the data series to draw
     * @param strokeWidth the stroke width of the path
     */
    public void drawLine(int axis, @NonNull DataSeries series, float strokeWidth) {
        if (series.size() > 0) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2 * strokeWidth * options.density);
            // dataBounds will be expanded in renderPath
            final Path line = renderPath(axis, series);
            canvas.drawPath(line, paint);
        }
    }

//    /**
//     * Draws a series of points (input given in screen-space)
//     * @param series the data series to draw
//     * @param y_zero the y-origin, in plot-space
//     */
//    void drawArea(@NonNull DataSeries series, double y_zero) {
//        if (series.size() > 0) {
//            Path area = renderArea(series, y_zero);
//            if (area != null) {
//                paint.setStyle(Paint.Style.FILL);
//                canvas.drawPath(area, paint);
//            }
//        }
//    }

    /**
     * Returns a path representing the data
     *
     * @param series the data series to draw
     */
    @NonNull
    private Path renderPath(int axis, @NonNull DataSeries series) {
        // Construct the path
        path.rewind();
        // restart is true if we should start a new line at the next point
        boolean restart = true;
        for (DataSeries.Point point : series) {
            if (!Double.isNaN(point.x) && !Double.isNaN(point.y)) {
                dataBounds[axis].expandBounds(point.x, point.y);
                final float sx = getX(axis, point.x);
                final float sy = getY(axis, point.y);
                if (restart) {
                    path.moveTo(sx, sy);
                    restart = false;
                } else {
                    path.lineTo(sx, sy);
                }
            } else {
                restart = true;
            }
        }
        return path;
    }

//    /**
//     * Returns a filled path representing the data
//     * @param series the data series to draw
//     * @param y_zero the y-origin, in plot-space
//     */
//    private Path renderArea(@NonNull DataSeries series, double y_zero) {
//        // Construct the path
//        path.rewind();
//        boolean empty = true;
//        double x = Double.NaN;
//        for (DataSeries.Point point : series) {
//            if (!Double.isNaN(point.x) && !Double.isNaN(point.y)) {
//                if (empty) {
//                    path.moveTo(getX(point.x), getY(y_zero));
//                    empty = false;
//                }
//                path.lineTo(getX(point.x), getY(point.y));
//                x = point.x; // Save last good x for later
//            }
//        }
//        if (!empty) {
//            // Log.w(TAG, "x=" + getX(x) + ", y_zero=" + getY(y_zero));
//            path.lineTo(getX(x), getY(y_zero));
//            path.close();
//            return path;
//        } else {
//            return null;
//        }
//    }

    /**
     * Returns the screen-space x coordinate
     */
    float getX(int axis, double x) {
        final IntBounds padding = options.padding;
        final double ppm_x = (width - padding.right - padding.left) / (bounds[axis].x.max - bounds[axis].x.min); // pixels per meter
        return (float) (padding.left + (x - bounds[axis].x.min) * ppm_x);
    }

    public float getX(double x) {
        return getX(0, x);
    }

    /**
     * Returns the data-space x coordinate from a screen-space x coordinate
     */
    double getXinverse(int axis, double sx) {
        final IntBounds padding = options.padding;
        final double ppm_x = (width - padding.right - padding.left) / (bounds[axis].x.max - bounds[axis].x.min); // pixels per meter
        return (sx - padding.left) / ppm_x + bounds[axis].x.min;
    }

    /**
     * Returns the screen-space y coordinate
     */
    float getY(int axis, double y) {
        final IntBounds padding = options.padding;
        final double ppm_y = (height - padding.bottom - padding.top) / (bounds[axis].y.max - bounds[axis].y.min); // pixels per meter
        return (float) (height - padding.bottom - (y - bounds[axis].y.min) * ppm_y);
    }

    public float getY(double y) {
        return getY(0, y);
    }

    /**
     * Returns the data-space x coordinate from a screen-space x coordinate
     */
    double getYinverse(int axis, double sy) {
        final IntBounds padding = options.padding;
        final double ppm_y = (height - padding.bottom - padding.top) / (bounds[axis].y.max - bounds[axis].y.min); // pixels per meter
        return (-sy + height - padding.bottom) / ppm_y + bounds[axis].y.min;
    }

}
