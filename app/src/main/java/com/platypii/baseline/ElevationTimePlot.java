package com.platypii.baseline;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;

import com.platypii.baseline.data.Bounds;
import com.platypii.baseline.data.Convert;
import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.measurements.MAltitude;

public class ElevationTimePlot extends PlotView {

    private final DataSeries series = new DataSeries();
    private final long window = 15000; // The size of the view window, in milliseconds
    private final PlotMode mode = PlotMode.AREA;
    

    public ElevationTimePlot(Context context, AttributeSet attrs) {
        super(context, attrs);

        final float density = context.getResources().getDisplayMetrics().density;
        padding_top = (int) (14 * density);
        padding_bottom = (int) (6 * density);
        padding_left = (int) (2 * density);
        padding_right = (int) (36 * density);

        min.left = 0;
        min.right = window;
        max.bottom = -1;
        min.top = 33 * Convert.FT;
        
        x_major_units = 10000;
        y_major_units = 1 * Convert.FT;
    }

    @Override
    public void drawData(Canvas canvas) {
        series.reset();
        // Copy values to local history (so that we don't block while drawing circles)
        synchronized(MyAltimeter.history) {
            for(MAltitude alt : MyAltimeter.history) {
                double x = alt.millis - MainActivity.startTime;
                double y = alt.altitude;
                series.addPoint(x, y);
            }
        }
        // Draw data
        if(mode == PlotMode.DOT) {
            drawPoints(canvas, series, 3, 0xff0000ff);
            // drawPoint(canvas, series_raw, 1f, 0xff777788); // Raw altitude
        } else if(mode == PlotMode.LINE) {
            // paint.setShader(new LinearGradient(0, bottom, 0, (3 * top + bottom) / 4, 0xff112288, 0xff3388bb, TileMode.CLAMP));
            paint.setShader(new LinearGradient(0, getY(0), 0, getY(4000 * Convert.FT), 0xee112288, 0xcc3388bb, TileMode.CLAMP));
            drawLine(canvas, series, 2);
            paint.setShader(null);
        } else if(mode == PlotMode.AREA) {
            // paint.setShader(new LinearGradient(0, bottom, 0, (3 * top + bottom) / 4, 0xff112288, 0xcc3388bb, TileMode.CLAMP));
            paint.setShader(new LinearGradient(0, getY(0), 0, getY(4000 * Convert.FT), 0xee112299, 0xbb66aadd, TileMode.CLAMP));
            drawArea(canvas, series, 0, 1);
            paint.setShader(null);
        }
    }

    // Always show 60 seconds
    private final Bounds bounds = new Bounds();
    @Override
    public Bounds getBounds(int width, int height, Bounds dataBounds) {
        long currentTime = System.currentTimeMillis();
        long startTime = Math.max(0, currentTime - MainActivity.startTime - window);
        bounds.set(dataBounds);
        bounds.clean(min, max);
        bounds.set(startTime, bounds.top, startTime + window, bounds.bottom);
        return bounds;
    }
    
    @Override
    public String formatX(double x) {
        return Convert.time1((long)x);
    }
    @Override
    public String formatY(double y) {
        if(Math.abs(y) < EPSILON) {
            return "";
        } else {
            return Convert.distance(y);
        }
    }

}

