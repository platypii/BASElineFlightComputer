package com.platypii.baseline.views.charts;

import com.platypii.baseline.measurements.MSensor;
import com.platypii.baseline.util.AdjustBounds;
import com.platypii.baseline.util.Bounds;
import com.platypii.baseline.util.DataSeries;
import com.platypii.baseline.util.SyncedList;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import java.util.Iterator;

public class SensorPlot extends PlotView {

    private static final int AXIS_SENSOR = 0;
    private final Bounds bounds = new Bounds();

    private SyncedList<MSensor> history;

    private final DataSeries xSeries = new DataSeries();
    private final DataSeries ySeries = new DataSeries();
    private final DataSeries zSeries = new DataSeries();

    final Bounds inner = new Bounds();
    final Bounds outer = new Bounds();

    public SensorPlot(Context context, AttributeSet attrs) {
        super(context, attrs);

        final float density = getResources().getDisplayMetrics().density;
        options.padding.top = (int)(6 * density);
        options.padding.bottom = (int) (6 * density);
        options.padding.left = (int) (2 * density);
        options.padding.right = (int) (6 * density);

        inner.x.min = outer.x.min = 0;
        inner.x.max = 100;
        inner.y.min = -1;
        inner.y.max = 1;
    }

    /**
     * Load a list of sensor readings into the plot
     * @param history A list of sensor measurements
     */
    public void loadHistory(SyncedList<MSensor> history) {
        this.history = history;
    } 

    @Override
    public void drawData(@NonNull Plot plot) {
        if(history != null) {
            xSeries.reset();
            ySeries.reset();
            zSeries.reset();
            // Copy values to data series (so that we don't block while drawing circles)
            synchronized(history) {
                final Iterator<MSensor> it = history.iterator();
                for(int i = 0; it.hasNext(); i++) {
                    MSensor event = it.next();
                    xSeries.addPoint(i, event.x());
                    ySeries.addPoint(i, event.y());
                    zSeries.addPoint(i, event.z());
                }
            }

            // Point plot:
            // drawPoints(canvas, xSeries, 1.5f, 0xffee0000);
            // drawPoints(canvas, ySeries, 1.5f, 0xff00ee00);
            // drawPoints(canvas, zSeries, 1.5f, 0xffee00ee);

            // Area plot:
            // paint.setColor(0xffee0000);
            // drawArea(canvas, xSeries, 0, 1);
            // paint.setColor(0xff00ee00);
            // drawArea(canvas, ySeries, 0, 1);
            // paint.setColor(0xffee00ee);
            // drawArea(canvas, zSeries, 0, 1);

            // Line plot:
            paint.setStrokeMiter(2);
            paint.setColor(0xffee0000);
            plot.drawLine(AXIS_SENSOR, xSeries, 1.5f, paint);
            paint.setColor(0xff00ee00);
            plot.drawLine(AXIS_SENSOR, ySeries, 1.5f, paint);
            paint.setColor(0xffee00ee);
            plot.drawLine(AXIS_SENSOR, zSeries, 1.5f, paint);
        }
    }

    @NonNull
    @Override
    public Bounds getBounds(@NonNull Bounds dataBounds, int axis) {
        // Show last N
        bounds.set(dataBounds);
        AdjustBounds.clean(bounds, inner, outer);
        // Symmetric Y axis
        final double topBottom = Math.max(Math.abs(bounds.y.max), Math.abs(bounds.y.min));
        bounds.set(bounds.x.min, topBottom, bounds.x.max, -topBottom);
        return bounds;
    }

}
