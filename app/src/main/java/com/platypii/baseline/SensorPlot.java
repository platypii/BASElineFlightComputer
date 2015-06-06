package com.platypii.baseline;

import com.platypii.baseline.data.Bounds;
import com.platypii.baseline.data.SyncedList;
import com.platypii.baseline.data.measurements.MSensor;

import java.util.Iterator;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;


public class SensorPlot extends PlotView {

    private SyncedList<MSensor> history;

    private final DataSeries xSeries = new DataSeries();
    private final DataSeries ySeries = new DataSeries();
    private final DataSeries zSeries = new DataSeries();
    private final PlotMode mode = PlotMode.LINE;

    public SensorPlot(Context context, AttributeSet attrs) {
        super(context, attrs);

        float density = getResources().getDisplayMetrics().density;
        padding_top = (int)(6 * density);
        padding_bottom = (int) (6 * density);
        padding_left = (int) (2 * density);
        padding_right = (int) (6 * density);

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
                Iterator<MSensor> it = history.iterator();
                for(int i = 0; it.hasNext(); i++) {
                    MSensor event = it.next();
                    xSeries.addPoint(i, event.x());
                    ySeries.addPoint(i, event.y());
                    zSeries.addPoint(i, event.z());
                }
            }
            // Draw data
            if(mode == PlotMode.DOT) {
                drawPoints(canvas, xSeries, 1.5f, 0xffee0000);
                drawPoints(canvas, ySeries, 1.5f, 0xff00ee00);
                drawPoints(canvas, zSeries, 1.5f, 0xffee00ee);
            } else if(mode == PlotMode.LINE) {
                paint.setStrokeMiter(2);
                paint.setColor(0xffee0000);
                drawLine(canvas, xSeries, 1.5f);
                paint.setColor(0xff00ee00);
                drawLine(canvas, ySeries, 1.5f);
                paint.setColor(0xffee00ee);
                drawLine(canvas, zSeries, 1.5f);
            } else if(mode == PlotMode.AREA) {
                paint.setColor(0xffee0000);
                drawArea(canvas, xSeries, 0, 1);
                paint.setColor(0xff00ee00);
                drawArea(canvas, ySeries, 0, 1);
                paint.setColor(0xffee00ee);
                drawArea(canvas, zSeries, 0, 1);
            }
        }
    }

    private final Bounds bounds = new Bounds();
    @Override
    public Bounds getBounds(int width, int height, Bounds dataBounds) {
        // Show last N
        bounds.set(dataBounds);
        bounds.clean(min, max);
        // Symmetric Y axis
        double topBottom = Math.max(Math.abs(bounds.top), Math.abs(bounds.bottom));
        bounds.set(bounds.left, topBottom, bounds.right, -topBottom);
        // bounds.set(bounds.right - N, topBottom, bounds.right, -topBottom);
        return bounds;
    }

    @Override
    public String formatX(double x) {
        return "";
    }
    @Override
    public String formatY(double y) {
        return "";
    }

}

