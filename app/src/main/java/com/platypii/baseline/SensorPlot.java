package com.platypii.baseline;

import com.platypii.baseline.measurements.MSensor;
import com.platypii.baseline.util.Bounds;
import com.platypii.baseline.util.DataSeries;
import com.platypii.baseline.util.SyncedList;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import java.util.Iterator;

public class SensorPlot extends PlotView {

    private SyncedList<MSensor> history;

    private final DataSeries xSeries = new DataSeries();
    private final DataSeries ySeries = new DataSeries();
    private final DataSeries zSeries = new DataSeries();

    public SensorPlot(Context context, AttributeSet attrs) {
        super(context, attrs);

        final float density = getResources().getDisplayMetrics().density;
        padding.top = (int)(6 * density);
        padding.bottom = (int) (6 * density);
        padding.left = (int) (2 * density);
        padding.right = (int) (6 * density);

        min.left = 0;
        min.right = 100;
        max.bottom = -1;
        min.top = 1;

        x_major_units = 1;
        y_major_units = 1;
    }

    /**
     * Load a list of sensor readings into the plot
     * @param history A list of sensor measurements
     */
    public void loadHistory(SyncedList<MSensor> history) {
        this.history = history;
    } 

    @Override
    public void drawData(Canvas canvas) {
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
            drawLine(canvas, xSeries, 1.5f);
            paint.setColor(0xff00ee00);
            drawLine(canvas, ySeries, 1.5f);
            paint.setColor(0xffee00ee);
            drawLine(canvas, zSeries, 1.5f);
        }
    }

    private final Bounds bounds = new Bounds();
    @Override
    public Bounds getBounds(Bounds dataBounds) {
        // Show last N
        bounds.set(dataBounds);
        bounds.clean(min, max);
        // Symmetric Y axis
        final double topBottom = Math.max(Math.abs(bounds.top), Math.abs(bounds.bottom));
        bounds.set(bounds.left, topBottom, bounds.right, -topBottom);
        // bounds.set(bounds.right - N, topBottom, bounds.right, -topBottom);
        return bounds;
    }

}

