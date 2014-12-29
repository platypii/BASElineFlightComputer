package com.platypii.baseline.ui;

import com.platypii.baseline.data.Bounds;
import com.platypii.baseline.data.MyLocation;
import com.platypii.baseline.data.MyLocationManager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;


public class GlidePlot extends PlotView {

    private final DataSeries series = new DataSeries();
    private final long window = 20000; // The size of the view window, in milliseconds
    private final PlotMode mode = PlotMode.LINE;

    
    public GlidePlot(Context context, AttributeSet attrs) {
		super(context, attrs);

	    float density = getResources().getDisplayMetrics().density;
        padding_top = (int)(12 * density);
        padding_bottom = (int) (1.6f * density);
        padding_left = (int) (2 * density);
        padding_right = (int) (24 * density);
        
        min.left = 0;
        min.right = window;
        min.bottom = 0;
        max.bottom = 0;
        min.top = 1.5;
        
        x_major_units = 10000;
        y_major_units = 1;
    }
	
    @Override
    public void drawData(Canvas canvas) {
        long currentTime = System.currentTimeMillis();
        series.reset();
        // Copy values to local history (so that we don't block while drawing circles)
        synchronized(MyLocationManager.history) {
            for(MyLocation loc : MyLocationManager.history) {
            	if(currentTime - window - 10000 <= loc.timeMillis) {
	                double x = loc.timeMillis - MainActivity.startTime;
	                double y = loc.glideRatio();
	                series.addPoint(x, y);
            	}
            }
        }
        // Draw data
        if(mode == PlotMode.DOT) {
            drawPoints(canvas, series, 3, 0xff0000ff);
        } else if(mode == PlotMode.LINE) {
            paint.setShader(new LinearGradient(0, getY(Double.POSITIVE_INFINITY), 0, bottom, 0xff1122dd, 0xff5599bb, TileMode.CLAMP));
            drawLine(canvas, series, 2);
            paint.setShader(null);
        } else if(mode == PlotMode.AREA) {
            paint.setShader(new LinearGradient(0, getY(Double.POSITIVE_INFINITY), 0, bottom, 0xff1122dd, 0xff5599bb, TileMode.CLAMP));
            drawArea(canvas, series, Double.POSITIVE_INFINITY, 1);
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
    public void drawYlines(Canvas canvas, Bounds realBounds) {
        for(int glide = -20; glide <= 20; glide++) {
            if(-3 <= glide && glide <= 0)
                drawYline(canvas, glide, 0, 0xff999999, Convert.glide(-glide, 0));
            else
                drawYline(canvas, glide, 0, 0xff999999, "");
        }
        drawYline(canvas, Double.POSITIVE_INFINITY, 0, 0xffdddddd, "Level");
    }
    
    // Returns the screen-space y coordinate
    @Override
    public float getY(double y) {
        int zero = (top + padding_top);
        int height = (bottom - padding_bottom) - zero;
        if(Double.isInfinite(y)) {
            // Level
            return zero;
        } else if(0 < y) {
            return (float) (zero - height / (1 + y * y * 0.5));
        } else {
            return (float) (zero + height / (1 + y * y * 0.5));
        }
    }

    @Override
    public String formatX(double x) {
        return "";
    }
    @Override
    public String formatY(double y) {
        return Convert.glide(y);
    }

}

