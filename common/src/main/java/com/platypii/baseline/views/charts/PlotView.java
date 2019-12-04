package com.platypii.baseline.views.charts;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.NonNull;

/**
 * A general view for plotting data.
 * Override methods to provide a data source, and customize the input/output.
 * Override getBounds to determine the mapping from data-space to screen-space.
 *
 * The way we handle data bounds is weird but fast. For each pass of the data we expand databounds.
 * At the beginning of each new drawing cycle we copy databounds to bounds, and use that to scale.
 */
public abstract class PlotView extends View implements BasePlot {

    // Plot drawing options
    private final float density = getResources().getDisplayMetrics().density;
    private final float fontscale = getResources().getConfiguration().fontScale;
    final PlotOptions options = new PlotOptions(density, fontscale);

    // Object to store the plot state and drawing primitives
    final Plot plot = new Plot(options);

    public PlotView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Initialize bounds
        plot.initBounds(1);
    }

    @NonNull
    @Override
    public Plot getPlot() {
        return plot;
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

}
