package com.platypii.baseline.views.charts;

import com.platypii.baseline.util.Bounds;
import com.platypii.baseline.util.Exceptions;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * A general view for plotting data.
 * Override methods to provide a data source, and customize the input/output.
 * Override getBounds to determine the mapping from data-space to screen-space.
 *
 * The way we handle data bounds is weird but fast. For each pass of the data we expand databounds.
 * At the beginning of each new drawing cycle we copy databounds to bounds, and use that to scale.
 */
public abstract class PlotView extends SurfaceView implements SurfaceHolder.Callback {

    // Plot drawing options
    private final float density = getResources().getDisplayMetrics().density;
    final PlotOptions options = new PlotOptions(density);
    private final PlotAxes axes = new PlotAxes(options);

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
        axes.drawGridlines(plot);

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

}
