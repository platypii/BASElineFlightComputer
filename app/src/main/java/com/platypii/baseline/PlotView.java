package com.platypii.baseline;

import com.platypii.baseline.data.Bounds;
import com.platypii.baseline.data.DataSeries;
import com.platypii.baseline.data.DataSeries.Point;
import com.platypii.baseline.data.IntBounds;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * A general view for plotting data.
 * Override methods to provide a data source, and customize the input/output.
 * Override getBounds to determine the mapping from data-space to screen-space.
 */
public abstract class PlotView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "Plot";

    // Drawing stuff
    private final static int axis_color = 0xffee0000;
    private final static int grid_color = 0xff777777;
    private final static float font_size = 16;

    // Padding
    IntBounds padding = new IntBounds(0,0,0,0);

    double x_major_units = 1;
    double y_major_units = 1;

    // Default bounds
    final Bounds min = new Bounds(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
    final Bounds max = new Bounds(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    // Avoid creating new objects unnecessarily
    final Paint paint = new Paint();
    final Paint text = new Paint();
    private final Path path = new Path();

    final float density = getResources().getDisplayMetrics().density;
    final double EPSILON = 0.001;

    // THE FOLLOWING FIELDS ARE ONLY VALID IN THE PLOTVIEW.DRAWPLOT() CONTEXT:
    // View bounds lag behind data bounds by 1 refresh. Faster.
    private Bounds bounds; // The current view bounds
    private final Bounds dataBounds = new Bounds(); // the data bounds from the last draw

    // The current view bounds (in screen space)
    int bottom = 100;
    int top = 0;
    int left = 0;
    int right = 200;

    public PlotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        // drawingThread = new DrawingThread(holder);
        paint.setAntiAlias(true);
        paint.setDither(true);
        text.setAntiAlias(true);
        text.setTextSize(font_size * density);
    }

    // SurfaceView stuff:
    // Secondary drawing thread
    private DrawingThread drawingThread;
    private class DrawingThread extends Thread {
        private final SurfaceHolder _surfaceHolder;
        private boolean running = false;
        DrawingThread(final SurfaceHolder surfaceHolder) {
            _surfaceHolder = surfaceHolder;
        }
        @Override
        public void run() {
            while(running) {
                Canvas canvas = null;
                try {
                    canvas = _surfaceHolder.lockCanvas();
                    if(canvas != null) {
                        synchronized (_surfaceHolder) {
                            drawPlot(canvas);
                        }
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown, we don't leave the Surface in an inconsistent state
                    if(canvas != null) {
                        _surfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }
    }
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
    public void surfaceCreated(SurfaceHolder holder) {
        drawingThread = new DrawingThread(holder);
        drawingThread.running = true;
        drawingThread.start();
    }
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        drawingThread.running = false;
        while(retry) {
            try {
                drawingThread.join();
                retry = false;
            } catch (InterruptedException e) {
                // we will try it again and again...
            }
        }
    }

    /**
     * Draw the plot (do not override lightly)
     */
    private void drawPlot(Canvas canvas) {
        bottom = getHeight();
        top = 0;
        left = 0;
        right = getWidth();

        // Get plot-space bounds
        bounds = getBounds(dataBounds);
        // Reset data bounds
        dataBounds.set(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        // Plot Area
        canvas.drawColor(0xff000000);

        // Draw grid lines
        text.setColor(0xffcccccc);
        drawGridlines(canvas);

        // Plot the data
        drawData(canvas);

        // Border box
        paint.setColor(0xeeaaaaaa);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Cap.SQUARE);
        paint.setStrokeWidth(2);
        canvas.drawRect(left, top, right, bottom, paint);
    }

    /**
     * Called when rendering the plot, must be overridden to draw the data.
     * Implementations should call drawPoint() and drawPath() to actually draw the data.
     */
    abstract void drawData(Canvas canvas);

    /**
     * Override this method to set the view bounds
     * @param dataBounds the data bounds from the last render pass
     * @return the view bounds, in plot-space
     */
    abstract Bounds getBounds(Bounds dataBounds);

    /**
     * Draws a point (input given in plot space)
     * @param canvas The canvas to draw on
     * @param radius The width of the path
     * @param color The color of the path
     */
    public void drawPoint(Canvas canvas, double x, double y, float radius, int color) {
        dataBounds.expandBounds(x, y);
        // Screen coordinates
        float sx = getX(x);
        float sy = getY(y);
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        // paint.setStrokeCap(Cap.ROUND); // doesn't work in hardware mode
        // canvas.drawPoint(x, y, paint);
        canvas.drawCircle(sx, sy, radius * density, paint);
    }

//    /**
//     * Draws a series of points as dots
//     * @param canvas The canvas to draw on
//     * @param series The data series to draw
//     * @param radius The width of the path
//     * @param color The color of the path
//     */
//    public void drawPoints(Canvas canvas, DataSeries series, float radius, int color) {
//        paint.setColor(color);
//        paint.setStyle(Paint.Style.FILL);
//        for(Point point : series) {
//            dataBounds.expandBounds(point.x, point.y);
//            canvas.drawCircle(getX(point.x), getY(point.y), radius * density, paint);
//        }
//    }

    /**
     * Draws a series of points (input given in screen space)
     * @param canvas The canvas to draw on
     * @param series The data series to draw
     * @param radius The width of the path
     */
    public void drawLine(Canvas canvas, DataSeries series, float radius) {
        if(series.size() > 0) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2 * radius * density);
            Path line = renderPath(series);
            if(line != null) {
                canvas.drawPath(line, paint);
            }
        }
    }

//    /**
//     * Draws a series of points (input given in screen space)
//     * @param canvas The canvas to draw on
//     * @param series The data series to draw
//     * @param y_zero The y-origin, in plot-space
//     * @param radius The width of the path
//     */
//    public void drawArea(Canvas canvas, DataSeries series, double y_zero, float radius) {
//        if(series.size() > 0) {
//            Path area = renderArea(series, y_zero);
//            if(area != null) {
//                paint.setStyle(Paint.Style.FILL);
//                canvas.drawPath(area, paint);
//                if(radius > 0) {
//                    paint.setStyle(Paint.Style.STROKE);
//                    paint.setStrokeWidth(radius);
//                    canvas.drawPath(area, paint);
//                }
//            }
//        }
//    }

    /**
     * Returns a path representing the data
     * @param series The data series to draw
     */
    private Path renderPath(DataSeries series) {
        // Construct the path
        path.rewind();
        boolean empty = true;
        for(Point point : series) {
            dataBounds.expandBounds(point.x, point.y);
            if(!Double.isNaN(point.x) && !Double.isNaN(point.y)) {
                if(empty) {
                    path.moveTo(getX(point.x), getY(point.y));
                    empty = false;
                } else {
                    path.lineTo(getX(point.x), getY(point.y));
                }
            }
        }
        return path;
    }

//    /**
//     * Returns a filled path representing the data
//     * @param series The data series to draw
//     * @param y_zero The y-origin, in plot-space
//     */
//    private Path renderArea(DataSeries series, double y_zero) {
//        // Construct the path
//        path.rewind();
//        boolean empty = true;
//        double x = Double.NaN;
//        for(Point point : series) {
//            if(!Double.isNaN(point.x) && !Double.isNaN(point.y)) {
//                dataBounds.expandBounds(point.x, point.y);
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

    // GRID LINES
    /**
     * Called when rendering the plot, must be overridden to draw the grid lines. Implementations may find drawXline() and drawYline() quite useful.
     */
    public void drawGridlines(Canvas canvas) {
        Bounds realBounds = getRealBounds();
        drawXlines(canvas, realBounds);
        drawYlines(canvas, realBounds);
    }

    /**
     * Draw the grid lines for the current view between the given screen-space coordinates
     * Default behavior is to draw grid lines for the nearest order of magnitude along the axis
     */
    private static final int MAX_LINES = 80;
    public void drawXlines(Canvas canvas, Bounds realBounds) {
        final int magnitude_x = (int) Math.log10((realBounds.right - realBounds.left) / x_major_units);
        final double step_x = x_major_units * pow(10, magnitude_x);
        final double start_x = Math.floor(realBounds.left / step_x) * step_x;
        final double end_x = Math.ceil(realBounds.right / step_x) * step_x;
        final int steps_x = (int)Math.ceil((end_x - start_x) / step_x);
        if (!(start_x < end_x && 0 < step_x)) {
            Log.e(TAG, "Invalid plot bounds " + start_x + " " + end_x + " " + step_x);
        }
        for(int n = 0; n < steps_x; n++) {
            final double x = start_x + n * step_x;
            if(Math.abs(x) < EPSILON) {
                drawXline(canvas, x, 0, axis_color, formatX(x));
            } else {
                drawXline(canvas, x, 0, grid_color, formatX(x));
            }

            if(n > MAX_LINES) {
                Log.e(TAG, "Too many X grid lines!");
                break;
            }
        }
    }
    public void drawYlines(Canvas canvas, Bounds realBounds) {
        final int magnitude_y = (int) Math.log10((realBounds.top - realBounds.bottom) / y_major_units);
        final double step_y = y_major_units * pow(10, magnitude_y); // grid spacing in plot-space
        final double start_y = Math.floor(realBounds.bottom / step_y) * step_y;
        final double end_y = Math.ceil(realBounds.top / step_y) * step_y;
        final int steps_y = (int)Math.ceil((end_y - start_y) / step_y);
        // Log.i(TAG, "bounds = " + bounds + ", realBounds = " + realBounds);
        // Log.i(TAG, "start_y = " + start_y + ", end_y = " + end_y + ", magnitude_y = " + magnitude_y + ", step_y = " + step_y);
        if (!(start_y <= end_y && 0 < step_y)) {
            Log.e(TAG, "Invalid plot bounds " + start_y + " " + end_y + " " + step_y);
        }
        for(int n = 0; n < steps_y; n++) {
            final double y = start_y + n * step_y;
            if(Math.abs(y) < EPSILON) {
                drawYline(canvas, y, 0, axis_color, formatY(y));
            } else {
                drawYline(canvas, y, 0, grid_color, formatY(y));
            }

            if(n > MAX_LINES) {
                Log.e(TAG, "Too many Y grid lines!");
                break;
            }
        }
    }

    /**
     * Draws an X grid line (vertical)
     */
    public void drawXline(Canvas canvas, double x, float width, int color, String label) {
        // Screen coordinate
        final int sx = (int) getX(x);
        paint.setColor(color);
        paint.setStrokeWidth(width);
        canvas.drawLine(sx, top, sx, bottom, paint);
        if(label != null) {
            text.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(label, sx + 2 * density, top + 10 * density, text);
        }
    }
    /**
     * Draws a Y grid line (horizontal)
     */
    public void drawYline(Canvas canvas, double y, float width, int color, String label) {
        final int sy = (int) getY(y);
        paint.setColor(color);
        paint.setStrokeWidth(width);
        canvas.drawLine(left, sy, right, sy, paint);
        if(label != null) {
            // Left align
            canvas.drawText(label, left + 2 * density, sy - 2 * density, text);
            // Right align
            // text.setTextAlign(Paint.Align.RIGHT);
            // canvas.drawText(label, right - 2 * density, sy - 2 * density, text);
        }
    }

    public void drawXtick(Canvas canvas, double x, String label) {
        final int sx = (int) getX(x);
        final int sy = (int) getY(0);
        canvas.drawLine(sx, sy - 4 * density, sx, sy + 4 * density, paint);
        text.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(label, sx, sy + 19 * density, text);
    }
    public void drawYtick(Canvas canvas, double y, String label) {
        final int sx = (int) getX(0);
        final int sy = (int) getY(y);
        canvas.drawLine(sx - 4 * density, sy, sx + 4 * density, sy, paint);
        text.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(label, sx + 7 * density, sy + 6 * density, text);
    }

    // Override this to change how labels are displayed
    public String formatX(double x) {
        return null;
    }
    public String formatY(double y) {
        return null;
    }

    // Returns the bounds in plot-space, including padding
    private final Bounds realBounds = new Bounds();
    public Bounds getRealBounds() {
        final double ppm_x = ((right - padding.right) - (left + padding.left)) / (bounds.right - bounds.left); // pixels per meter
        final double rLeft = bounds.left - padding.left / ppm_x; // min x-coordinate in plot-space
        final double rRight = bounds.left + (right - (left + padding.left)) / ppm_x; // max x-coordinate in plot-space
        final double ppm_y = ((bottom - padding.bottom) - (top + padding.top)) / (bounds.top - bounds.bottom); // pixels per meter
        final double rBottom = bounds.bottom - padding.bottom / ppm_y; // min y-coordinate in plot-space
        final double rTop = bounds.bottom - (top - (bottom - padding.bottom)) / ppm_y; // max y-coordinate in plot-space
        realBounds.set(rLeft, rTop, rRight, rBottom);
        return realBounds;
    }

    /**
     * Fast integer power x^y
     */
    private static int pow(int x, int y) {
        // base cases
        if(x == 1 || y == 0) return 1;
        else if(y == 1) return x;
        else if(y == 2) return x * x;
        else if(y == 3) return x * x * x;
        // divide and conquer
        final int sqrt = pow(x, y / 2);
        if(y % 2 == 0) return sqrt * sqrt;
        else return x * sqrt * sqrt;
    }

    /**
     * Returns the screen-space x coordinate
     */
    float getX(double x) {
        final double ppm_x = ((right - padding.right) - (left + padding.left)) / (bounds.right - bounds.left); // pixels per meter
        return (float) (left + padding.left + (x - bounds.left) * ppm_x);
    }

    /**
     * Returns the screen-space y coordinate
     */
    float getY(double y) {
        final double ppm_y = ((bottom - top - padding.bottom) - (top + padding.top)) / (bounds.top - bounds.bottom); // pixels per meter
        return (float) (bottom - padding.bottom - (y - bounds.bottom) * ppm_y);
    }

}
