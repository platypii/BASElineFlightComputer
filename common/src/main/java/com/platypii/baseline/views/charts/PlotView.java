package com.platypii.baseline.views.charts;

import com.platypii.baseline.util.Bounds;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.util.IntBounds;
import com.platypii.baseline.util.Numbers;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
    private static final String TAG = "PlotView";

    // Plot drawing options
    private final float density = getResources().getDisplayMetrics().density;
    final PlotOptions options = new PlotOptions(density);

    // Object to store the plot state and drawing primitives
    final Plot plot = new Plot(options);

    // The drawing thread will sleep for refreshRateMillis
    private static final long refreshRateMillis = 33; // Approx 30fps

    // Avoid creating new objects unnecessarily
    final Paint paint = new Paint();
    final Paint text = new Paint();

    final double EPSILON = 0.001;

    public PlotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        final SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        // drawingThread = new DrawingThread(holder);
        paint.setAntiAlias(true);
        paint.setDither(true);
        text.setAntiAlias(true);
        text.setTextSize(options.font_size * options.density);
        text.setColor(0xffcccccc);
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
                        plot.setCanvas(canvas);
                        synchronized (_surfaceHolder) {
                            drawPlot(plot);
                        }
                    }
                } catch(Exception e) {
                    Exceptions.report(e);
                } finally {
                    // do this in a finally so that if an exception is thrown, we don't leave the Surface in an inconsistent state
                    if(canvas != null) {
                        try {
                            _surfaceHolder.unlockCanvasAndPost(canvas);
                        } catch(Exception e) {
                            // _surfaceHolder.getSurface().isValid()?
                            Exceptions.report(new Exception("Crash while unlocking canvas: " + canvas, e));
                        }
                    }
                }
                // Frame limiting
                try {
                    Thread.sleep(refreshRateMillis);
                } catch (InterruptedException ignored) {}
            }
        }
    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        drawingThread = new DrawingThread(holder);
        drawingThread.running = true;
        drawingThread.start();
    }
    @Override
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
    private void drawPlot(@NonNull Plot plot) {
        // Get plot-space bounds from previous frame's dataBounds
        plot.bounds.set(getBounds(plot.dataBounds));
        // Reset data bounds
        plot.dataBounds.reset();

        // Background
        plot.canvas.drawColor(0xff000000);

        // Draw grid lines
        drawGridlines(plot);

        // Plot the data
        drawData(plot);
    }

    /**
     * Called when rendering the plot, must be overridden to draw the data.
     * Implementations should call drawPoint() and drawPath() to actually draw the data.
     */
    abstract void drawData(Plot plot);

    /**
     * Override this method to set the view bounds
     * @param dataBounds the data bounds from the last render pass
     * @return the view bounds, in plot-space
     */
    abstract Bounds getBounds(Bounds dataBounds);

    // GRID LINES
    /**
     * Called when rendering the plot, must be overridden to draw the grid lines. Implementations may find drawXline() and drawYline() quite useful.
     */
    public void drawGridlines(@NonNull Plot plot) {
        Bounds realBounds = getRealBounds(plot);
        drawXlines(plot, realBounds);
        drawYlines(plot, realBounds);
    }

    /**
     * Draw the grid lines for the current view between the given screen-space coordinates
     * Default behavior is to draw grid lines for the nearest order of magnitude along the axis
     */
    private static final int MAX_LINES = 80;
    public void drawXlines(@NonNull Plot plot, @NonNull Bounds realBounds) {
        final int magnitude_x = (int) Math.log10((realBounds.x.max - realBounds.x.min) / options.axis.x.major_units);
        final double step_x = options.axis.x.major_units * Numbers.pow(10, magnitude_x);
        final double start_x = Math.floor(realBounds.x.min / step_x) * step_x;
        final double end_x = Math.ceil(realBounds.x.max / step_x) * step_x;
        final int steps_x = (int)Math.ceil((end_x - start_x) / step_x);
        if (!(start_x < end_x && 0 < step_x)) {
            Log.e(TAG, "Invalid plot X bounds " + start_x + " " + end_x + " " + step_x);
        }
        for(int n = 0; n < steps_x; n++) {
            final double x = start_x + n * step_x;
            if(Math.abs(x) < EPSILON) {
                drawXline(plot, x, options.axis_color, options.axis.x.format(x));
            } else {
                drawXline(plot, x, options.grid_color, options.axis.x.format(x));
            }

            if(n > MAX_LINES) {
                Log.e(TAG, "Too many X grid lines!");
                break;
            }
        }
    }
    public void drawYlines(@NonNull Plot plot, @NonNull Bounds realBounds) {
        final int magnitude_y = (int) Math.log10((realBounds.y.max - realBounds.y.min) / options.axis.y.major_units);
        final double step_y = options.axis.y.major_units * Numbers.pow(10, magnitude_y); // grid spacing in plot-space
        final double start_y = Math.floor(realBounds.y.min / step_y) * step_y;
        final double end_y = Math.ceil(realBounds.y.max / step_y) * step_y;
        final int steps_y = (int)Math.ceil((end_y - start_y) / step_y);
        // Log.i(TAG, "bounds = " + bounds + ", realBounds = " + realBounds);
        // Log.i(TAG, "start_y = " + start_y + ", end_y = " + end_y + ", magnitude_y = " + magnitude_y + ", step_y = " + step_y);
        if (!(start_y <= end_y && 0 < step_y)) {
            Log.e(TAG, "Invalid plot Y bounds " + start_y + " " + end_y + " " + step_y);
        }
        for(int n = 0; n < steps_y; n++) {
            final double y = start_y + n * step_y;
            if(Math.abs(y) < EPSILON) {
                drawYline(plot, y, options.axis_color, options.axis.y.format(y));
            } else {
                drawYline(plot, y, options.grid_color, options.axis.y.format(y));
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
    public void drawXline(@NonNull Plot plot, double x, int color, @Nullable String label) {
        // Screen coordinate
        final int sx = (int) plot.getX(x);
        paint.setColor(color);
        paint.setStrokeWidth(0);
        plot.canvas.drawLine(sx, 0, sx, getHeight(), paint);
        if(label != null) {
            text.setTextAlign(Paint.Align.LEFT);
            plot.canvas.drawText(label, sx + 2 * options.density, 10 * options.density, text);
        }
    }
    /**
     * Draws a Y grid line (horizontal)
     */
    public void drawYline(@NonNull Plot plot, double y, int color, @Nullable String label) {
        final int sy = (int) plot.getY(y);
        paint.setColor(color);
        paint.setStrokeWidth(0);
        plot.canvas.drawLine(0, sy, getWidth(), sy, paint);
        if(label != null) {
            // Left align
            plot.canvas.drawText(label, 2 * options.density, sy - 2 * options.density, text);
            // Right align
            // text.setTextAlign(Paint.Align.RIGHT);
            // canvas.drawText(label, right - 2 * density, sy - 2 * density, text);
        }
    }

//    public void drawXtick(Canvas canvas, double x, String label) {
//        final int sx = (int) getX(x);
//        final int sy = (int) getY(0);
//        canvas.drawLine(sx, sy - 4 * density, sx, sy + 4 * density, paint);
//        text.setTextAlign(Paint.Align.CENTER);
//        canvas.drawText(label, sx, sy + 19 * density, text);
//    }
//    public void drawYtick(Canvas canvas, double y, String label) {
//        final int sx = (int) getX(0);
//        final int sy = (int) getY(y);
//        canvas.drawLine(sx - 4 * density, sy, sx + 4 * density, sy, paint);
//        text.setTextAlign(Paint.Align.LEFT);
//        canvas.drawText(label, sx + 7 * density, sy + 6 * density, text);
//    }

    // Returns the bounds in plot-space, including padding
    private final Bounds realBounds = new Bounds();
    @NonNull
    private Bounds getRealBounds(Plot plot) {
        final IntBounds padding = options.padding;
        final double ppm_x = (getWidth() - padding.right - padding.left) / (plot.bounds.x.max - plot.bounds.x.min); // pixels per meter
        final double rLeft = plot.bounds.x.min - padding.left / ppm_x; // min x-coordinate in plot-space
        final double rRight = plot.bounds.x.min + (getWidth() - padding.left) / ppm_x; // max x-coordinate in plot-space
        final double ppm_y = (getHeight() - padding.bottom - padding.top) / (plot.bounds.y.max - plot.bounds.y.min); // pixels per meter
        final double rBottom = plot.bounds.y.min - padding.bottom / ppm_y; // min y-coordinate in plot-space
        final double rTop = plot.bounds.y.min + (getHeight() - padding.bottom) / ppm_y; // max y-coordinate in plot-space
        realBounds.set(rLeft, rTop, rRight, rBottom);
        return realBounds;
    }

}
