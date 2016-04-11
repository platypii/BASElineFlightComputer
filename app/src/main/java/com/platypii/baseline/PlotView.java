package com.platypii.baseline;

import com.platypii.baseline.data.Bounds;
import com.platypii.baseline.data.DataSeries;
import com.platypii.baseline.data.DataSeries.Point;

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
 * A general view for plotting data
 * Override methods to provide a data source, and customize the input/output
 * @author platypii
 */
public abstract class PlotView extends SurfaceView implements SurfaceHolder.Callback {

    // Drawing stuff
    int padding_top = 0;
    int padding_bottom = 0;
    int padding_left = 0;
    int padding_right = 0;

    double x_major_units = 1;
    double y_major_units = 1;

    // Default bounds
    final Bounds min = new Bounds(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
    final Bounds max = new Bounds(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);

    // Avoid creating new objects unnecessarily
    final Paint paint = new Paint();
    private final Paint text = new Paint();
    private final Path path = new Path();

    private final float density = getResources().getDisplayMetrics().density;
    final double EPSILON = 0.001;

    // THE FOLLOWING FIELDS ARE ONLY VALID IN THE PLOTVIEW.DRAWPLOT() CONTEXT:
    // View bounds lag behind data bounds by 1 refresh. Faster.
    private Bounds bounds; // The current view bounds
    private final Bounds dataBounds = new Bounds(); // the data bounds from the last draw

    // The current view bounds (in screen space)
    public int bottom = 100;
    public int top = 0;
    public int left = 0;
    public int right = 200;

    public enum PlotMode { DOT, LINE, AREA }

    public PlotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        // drawingThread = new DrawingThread(holder);
        paint.setAntiAlias(true);
        paint.setDither(true);
        text.setAntiAlias(true);
    }

    // SurfaceView stuff:
    // Secondary drawing thread
    private DrawingThread drawingThread;
    private class DrawingThread extends Thread {
        private final SurfaceHolder _surfaceHolder;
        private boolean running = false;
        public DrawingThread(final SurfaceHolder surfaceHolder) {
            _surfaceHolder = surfaceHolder;
        }
        @Override
        public void run() {
            Canvas canvas;
            while(running) {
                canvas = null;
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
        final int width = getWidth();
        final int height = getHeight();
        // density = getResources().getDisplayMetrics().density;

        bottom = height;
        top = 0;
        left = 0;
        right = width;

        // Get plot-space bounds
        bounds = getBounds(dataBounds);
        // Reset data bounds
        dataBounds.set(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        // Plot Area
        canvas.drawColor(0xff000000);

        // Draw grid lines
        text.setColor(0xffcccccc);
        text.setTextSize(10 * density); // 10dp
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
     * Return the view bounds in plot-space
     */
    abstract Bounds getBounds(Bounds dataBounds);

    /**
     * Draws a point (input given in plot space)
     * @param canvas The canvas to draw on
     * @param radius The width of the path
     * @param color The color of the path
     */
//    public void drawPoint(Canvas canvas, double x, double y, float radius, int color) {
//        dataBounds.expandBounds(x, y);
//        // Screen coordinates
//        float sx = getX(x);
//        float sy = getY(y);
//        paint.setColor(color);
//        paint.setStyle(Paint.Style.FILL);
//        // paint.setStrokeCap(Cap.ROUND); // doesn't work in hardware mode
//        // canvas.drawPoint(x, y, paint);
//        canvas.drawCircle(sx, sy, radius * density, paint);
//    }

    /**
     * Draws a series of points as dots
     * @param canvas The canvas to draw on
     * @param series The data series to draw
     * @param radius The width of the path
     * @param color The color of the path
     */
    public void drawPoints(Canvas canvas, DataSeries series, float radius, int color) {
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        for(Point point : series) {
            dataBounds.expandBounds(point.x, point.y);
            canvas.drawCircle(getX(point.x), getY(point.y), radius * density, paint);
        }
    }

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

    /**
     * Draws a series of points (input given in screen space)
     * @param canvas The canvas to draw on
     * @param series The data series to draw
     * @param y_zero The y-origin, in plot-space
     * @param radius The width of the path
     */
    public void drawArea(Canvas canvas, DataSeries series, double y_zero, float radius) {
        if(series.size() > 0) {
            Path area = renderArea(series, y_zero);
            if(area != null) {
                paint.setStyle(Paint.Style.FILL);
                canvas.drawPath(area, paint);
                if(radius > 0) {
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(radius);
                    canvas.drawPath(area, paint);
                }
            }
        }
    }

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

    /**
     * Returns a filled path representing the data
     * @param series The data series to draw
     * @param y_zero The y-origin, in plot-space
     */
    private Path renderArea(DataSeries series, double y_zero) {
        // Construct the path
        path.rewind();
        boolean empty = true;
        double x = Double.NaN;
        for(Point point : series) {
            if(!Double.isNaN(point.x) && !Double.isNaN(point.y)) {
                dataBounds.expandBounds(point.x, point.y);
                if(empty) {
                    path.moveTo(getX(point.x), getY(y_zero));
                    empty = false;
                }
                path.lineTo(getX(point.x), getY(point.y));
                x = point.x; // Save last good x for later
            }
        }
        if(!empty) {
            // Log.w("Render Area Plot", "x=" + getX(x) + ", y_zero=" + getY(y_zero));
            path.lineTo(getX(x), getY(y_zero));
            path.close();
            return path;
        } else {
            return null;
        }
    }

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
        int magnitude_x = (int) Math.log10((realBounds.right - realBounds.left) / x_major_units);
        double step_x = x_major_units * pow(10, magnitude_x);
        double start_x = Math.floor(realBounds.left / step_x) * step_x;
        double end_x = Math.ceil(realBounds.right / step_x) * step_x;
        int steps_x = (int)Math.ceil((end_x - start_x) / step_x);
        assert start_x < end_x && 0 < step_x;
        for(int n = 0; n < steps_x; n++) {
            double x = start_x + n * step_x;
            if(Math.abs(x) < EPSILON) {
                drawXline(canvas, x, 0, 0xffee0000, formatX(x));
            } else {
                drawXline(canvas, x, 0, 0xff999999, formatX(x));
            }

            if(n > MAX_LINES) {
                Log.e("PlotView", "Too many X grid lines!");
                break;
            }
        }
    }
    public void drawYlines(Canvas canvas, Bounds realBounds) {
        int magnitude_y = (int) Math.log10((realBounds.top - realBounds.bottom) / y_major_units);
        double step_y = y_major_units * pow(10, magnitude_y); // grid spacing in plot-space
        double start_y = Math.floor(realBounds.bottom / step_y) * step_y;
        double end_y = Math.ceil(realBounds.top / step_y) * step_y;
        int steps_y = (int)Math.ceil((end_y - start_y) / step_y);
        // Log.i("PlotView", "bounds = " + bounds + ", realBounds = " + realBounds);
        // Log.i("PlotView", "start_y = " + start_y + ", end_y = " + end_y + ", magnitude_y = " + magnitude_y + ", step_y = " + step_y);
        assert start_y <= end_y && 0 < step_y;
        for(int n = 0; n < steps_y; n++) {
            double y = start_y + n * step_y;
            if(Math.abs(y) < EPSILON) {
                drawYline(canvas, y, 0, 0xffee0000, formatY(y));
            } else {
                drawYline(canvas, y, 0, 0xff999999, formatY(y));
            }

            if(n > MAX_LINES) {
                Log.e("PlotView", "Too many Y grid lines!");
                break;
            }
        }
    }

    /**
     * Draws an X grid line (vertical)
     */
    public void drawXline(Canvas canvas, double x, float width, int color, String label) {
        // Screen coordinate
        int sx = (int) getX(x);
        paint.setColor(color);
        paint.setStrokeWidth(width);
        canvas.drawLine(sx, top, sx, bottom, paint);
        text.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(label, sx + 2 * density, top + 10 * density, text);
    }
    /**
     * Draws a Y grid line (horizontal)
     */
    public void drawYline(Canvas canvas, double y, float width, int color, String label) {
        int sy = (int) getY(y);
        paint.setColor(color);
        paint.setStrokeWidth(width);
        canvas.drawLine(left, sy, right, sy, paint);
        // Left align
        canvas.drawText(label, left + 2 * density, sy - 2 * density, text);
        // Right align
        // text.setTextAlign(Paint.Align.RIGHT);
        // canvas.drawText(label, right - 2 * density, sy - 2 * density, text);
    }

    // Override this to change how labels are displayed
    abstract public String formatX(double x);
    abstract public String formatY(double y);

    // Returns the bounds in plot-space, including padding
    private final Bounds realBounds = new Bounds();
    public Bounds getRealBounds() {
        double ppm_x = ((right - padding_right) - (left + padding_left)) / (bounds.right - bounds.left); // pixels per meter
        double rLeft = bounds.left - padding_left / ppm_x; // min x-coordinate in plot-space
        double rRight = bounds.left + (right - (left + padding_left)) / ppm_x; // max x-coordinate in plot-space
        double ppm_y = ((bottom - padding_bottom) - (top + padding_top)) / (bounds.top - bounds.bottom); // pixels per meter
        double rBottom = bounds.bottom - padding_bottom / ppm_y; // min y-coordinate in plot-space
        double rTop = bounds.bottom - (top - (bottom - padding_bottom)) / ppm_y; // max y-coordinate in plot-space
        realBounds.set(rLeft, rTop, rRight, rBottom);
        return realBounds;
    }

    // fast integer power x^y
    private int pow(int x, int y) {
        // base cases
        if(x == 1 || y == 0) return 1;
        int sqrt = pow(x, y / 2);
        if(y % 2 == 1) return x * sqrt * sqrt;
        else return sqrt * sqrt;
    }

    // Returns the screen-space x coordinate
    float getX(double x) {
        double ppm_x = ((right - padding_right) - (left + padding_left)) / (bounds.right - bounds.left); // pixels per meter
        return (float) (left + padding_left + (x - bounds.left) * ppm_x);
    }

    // Returns the screen-space y coordinate
    float getY(double y) {
        double ppm_y = ((bottom - padding_bottom) - (top + padding_top)) / (bounds.top - bounds.bottom); // pixels per meter
        return (float) (bottom - padding_bottom - (y - bounds.bottom) * ppm_y);
    }

}
