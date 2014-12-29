package com.platypii.baseline.ui;

import com.platypii.baseline.data.Bounds;
import com.platypii.baseline.data.MyLocation;
import com.platypii.baseline.data.MyLocationManager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;


public class SpeedPlot extends PlotView {

    private final DataSeries series = new DataSeries();
    private final long window = 20000; // The size of the view window, in milliseconds
    private final PlotMode mode = PlotMode.AREA;

    
    public SpeedPlot(Context context, AttributeSet attrs) {
        super(context, attrs);

        float density = getResources().getDisplayMetrics().density;
        padding_top = (int)(12 * density);
        padding_bottom = (int) (2 * density);
        padding_left = (int) (2 * density);
        padding_right = (int) (24 * density);
        
        min.left = 0;
        min.right = window;
        min.bottom = 0;
        max.bottom = 0;
        min.top = 22 * Convert.MPH;
        
        x_major_units = 10000;
        y_major_units = 1 * Convert.MPH;
    }

    @Override
    public void drawData(Canvas canvas) {
        long currentTime = System.currentTimeMillis();
        series.reset();
        // Copy values to data series (so that we don't block while drawing circles)
        synchronized(MyLocationManager.history) {
            for(MyLocation loc : MyLocationManager.history) {
            	if(currentTime - window - 10000 <= loc.timeMillis) {
	            	double x = loc.timeMillis - MainActivity.startTime;
	            	double y = loc.speed();
	                series.addPoint(x, y);
            	}
            }
        }
        // Draw data
        if(mode == PlotMode.DOT) {
            drawPoints(canvas, series, 3, 0xff0000ff);
        } else if(mode == PlotMode.LINE) {
            paint.setShader(new LinearGradient(0, getY(0), 0, getY(100 * Convert.MPH), 0xff1122ee, 0xff5599bb, TileMode.CLAMP));
            drawLine(canvas, series, 2);
            paint.setShader(null);
        } else if(mode == PlotMode.AREA) {
            paint.setShader(new LinearGradient(0, getY(0), 0, getY(100 * Convert.MPH), 0xff1122ee, 0xcc3399bb, TileMode.CLAMP));
            drawArea(canvas, series, 0, 1);
            paint.setShader(null);
        }
    }
    
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
        if(Math.abs(x) < EPSILON)
            return "";
        else
            return Convert.time1((long) x);
    }
    @Override
    public String formatY(double y) {
        if(Math.abs(y) < EPSILON)
            return "";
        else
            return Convert.speed(y, 0);
    }

}

