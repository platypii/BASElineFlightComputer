package com.platypii.baseline.views.charts;

import com.platypii.baseline.util.Bounds;
import com.platypii.baseline.util.DataSeries;
import com.platypii.baseline.util.IntBounds;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;

/**
 * Wraps a canvas, but operations are in plot-space x,y
 */
class Plot {

    // Drawing options
    private final PlotOptions options;

    // Drawing surface
    Canvas canvas;
    int width;
    int height;

    // Data bounds get expanded as we see points
    final Bounds dataBounds = new Bounds();
    // Plot bounds get updated once at the beginning of drawPlot
    final Bounds bounds = new Bounds();

    // Avoid creating new objects unnecessarily
    private final Path path = new Path();

    Plot(@NonNull PlotOptions options) {
        this.options = options;
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
     * Draws a point (input given in plot-space)
     * @param radius the width of the path
     * @param paint the paintbrush to use
     */
    void drawPoint(double x, double y, float radius, @NonNull Paint paint) {
        dataBounds.expandBounds(x, y);
        // Screen coordinates
        float sx = getX(x);
        float sy = getY(y);
        paint.setStyle(Paint.Style.FILL);
        // paint.setStrokeCap(Cap.ROUND); // doesn't work in hardware mode
        // canvas.drawPoint(x, y, paint);
        canvas.drawCircle(sx, sy, radius * options.density, paint);
    }

//    /**
//     * Draws a series of points as dots
//     * @param series the data series to draw
//     * @param radius the width of the path
//     */
//    void drawPoints(@NonNull DataSeries series, float radius, Paint paint) {
//        paint.setStyle(Paint.Style.FILL);
//        for(DataSeries.Point point : series) {
//            dataBounds.expandBounds(point.x, point.y);
//            canvas.drawCircle(getX(point.x), getY(point.y), radius * options.density, paint);
//        }
//    }

    /**
     * Draws a series of points (input given in plot-space)
     * @param series the data series to draw
     * @param radius the width of the path
     */
    void drawLine(@NonNull DataSeries series, float radius, @NonNull Paint paint) {
        if(series.size() > 0) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2 * radius * options.density);
            // dataBounds will be expanded in renderPath
            final Path line = renderPath(series);
            canvas.drawPath(line, paint);
        }
    }

//    /**
//     * Draws a series of points (input given in screen-space)
//     * @param series the data series to draw
//     * @param y_zero the y-origin, in plot-space
//     */
//    void drawArea(@NonNull DataSeries series, double y_zero, Paint paint) {
//        if(series.size() > 0) {
//            Path area = renderArea(series, y_zero);
//            if(area != null) {
//                paint.setStyle(Paint.Style.FILL);
//                canvas.drawPath(area, paint);
//            }
//        }
//    }

    /**
     * Returns a path representing the data
     * @param series the data series to draw
     */
    @NonNull
    private Path renderPath(@NonNull DataSeries series) {
        // Construct the path
        path.rewind();
        boolean empty = true;
        for(DataSeries.Point point : series) {
            if(!Double.isNaN(point.x) && !Double.isNaN(point.y)) {
                dataBounds.expandBounds(point.x, point.y);
                final float sx = getX(point.x);
                final float sy = getY(point.y);
                if(empty) {
                    path.moveTo(sx, sy);
                    empty = false;
                } else {
                    path.lineTo(sx, sy);
                }
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
//        for(DataSeries.Point point : series) {
//            if(!Double.isNaN(point.x) && !Double.isNaN(point.y)) {
//                if(empty) {
//                    path.moveTo(getX(point.x), getY(y_zero));
//                    empty = false;
//                }
//                path.lineTo(getX(point.x), getY(point.y));
//                x = point.x; // Save last good x for later
//            }
//        }
//        if(!empty) {
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
    float getX(double x) {
        final IntBounds padding = options.padding;
        final double ppm_x = (width - padding.right - padding.left) / (bounds.x.max - bounds.x.min); // pixels per meter
        return (float) (padding.left + (x - bounds.x.min) * ppm_x);
    }

    /**
     * Returns the screen-space y coordinate
     */
    float getY(double y) {
        final IntBounds padding = options.padding;
        final double ppm_y = (height - padding.bottom - padding.top) / (bounds.y.max - bounds.y.min); // pixels per meter
        return (float) (height - padding.bottom - (y - bounds.y.min) * ppm_y);
    }

}
