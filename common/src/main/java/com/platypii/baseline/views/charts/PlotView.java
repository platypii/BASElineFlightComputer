package com.platypii.baseline.views.charts;

import com.platypii.baseline.util.Bounds;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

/**
 * A general view for plotting data.
 * Override methods to provide a data source, and customize the input/output.
 * Override getBounds to determine the mapping from data-space to screen-space.
 *
 * The way we handle data bounds is weird but fast. For each pass of the data we expand databounds.
 * At the beginning of each new drawing cycle we copy databounds to bounds, and use that to scale.
 */
public abstract class PlotView extends View {

    // Plot drawing options
    private final float density = getResources().getDisplayMetrics().density;
    final PlotOptions options = new PlotOptions(density);
    private final PlotAxes axes = new PlotAxes(options);

    // Object to store the plot state and drawing primitives
    final Plot plot = new Plot(options);

    // Avoid creating new objects unnecessarily
    final Paint paint = new Paint();
    final Paint text = new Paint();

    public PlotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setAntiAlias(true);
        paint.setDither(true);
        text.setAntiAlias(true);
        text.setTextSize(options.font_size * options.density);
        text.setColor(0xffcccccc);
        // Initialize bounds
        plot.initBounds(1);
    }

    /**
     * Draw the plot (do not override lightly)
     */
    @Override
    public void onDraw(@NonNull Canvas canvas) {
        plot.setCanvas(canvas);
        // Draw twice because we need the data bounds
        drawPlot(plot);
        drawPlot(plot);
    }

    /**
     * Draw the plot (do not override lightly)
     */
    private void drawPlot(@NonNull Plot plot) {
        for (int i = 0; i < plot.bounds.length; i++) {
            // Get plot-space bounds from subclass, and copy to bounds
            plot.bounds[i].set(getBounds(plot.dataBounds[i], i));
            // Reset data bounds
            plot.dataBounds[i].reset();
        }

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
    abstract void drawData(@NonNull Plot plot);

    /**
     * Override this method to set the view bounds in plot-space.
     * It's okay for subclasses to use bounds as working space, or return dataBounds directly.
     * @param dataBounds the data bounds from the last render pass
     * @return the view bounds, in plot-space
     */
    @NonNull
    abstract Bounds getBounds(@NonNull Bounds dataBounds, int axis);

}
