package com.platypii.baseline.views.charts;

import com.platypii.baseline.util.Bounds;
import com.platypii.baseline.views.charts.layers.ChartLayer;

import androidx.annotation.NonNull;

interface BasePlot {

    /**
     * Add a chart layer
     *
     * @param layer chart layer to add
     */
    default void addLayer(@NonNull ChartLayer layer) {
        getPlot().layers.add(layer);
    }

    /**
     * Remove a chart layer
     *
     * @param layer chart layer to remove
     */
    default void removeLayer(@NonNull ChartLayer layer) {
        getPlot().layers.remove(layer);
    }

    /**
     * Draw the plot (do not override lightly)
     */
    default void drawPlot(@NonNull Plot plot) {
        plot.updateBounds(this);

        // Background
        plot.canvas.drawColor(plot.options.background_color);

        // Draw grid lines
        plot.axes.drawGridlines();

        // Draw the layers
        for (ChartLayer layer : plot.layers) {
            if (layer.isEnabled()) {
                layer.drawData(plot);
            }
        }

        // Plot the data
        drawData(plot);
    }

    @NonNull
    Plot getPlot();

    /**
     * Called when rendering the plot, must be overridden to draw the data.
     * Implementations should call drawPoint() and drawPath() to actually draw the data.
     */
    void drawData(@NonNull Plot plot);

    /**
     * Override this method to set the view bounds in plot-space.
     * It's okay for subclasses to use bounds as working space, or return dataBounds directly.
     *
     * @param dataBounds the data bounds from the last render pass
     * @return the view bounds, in plot-space
     */
    @NonNull
    Bounds getBounds(@NonNull Bounds dataBounds, int axis);

}
