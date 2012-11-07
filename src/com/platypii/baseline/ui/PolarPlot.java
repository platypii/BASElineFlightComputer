package com.platypii.baseline.ui;

import com.platypii.baseline.data.Bounds;
import com.platypii.baseline.data.MyLocation;
import com.platypii.baseline.data.MyLocationManager;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;


public class PolarPlot extends PlotView {

    private final long window = 60000; // The size of the view window, in milliseconds
    

    public PolarPlot(Context context, AttributeSet attrs) {
        super(context, attrs);

        float density = getResources().getDisplayMetrics().density;
        padding_top = (int) (16 * density);
        padding_bottom = (int) (16 * density);
        padding_left = (int) (2 * density);
        padding_right = (int) (18 * density);
        
        min.left = max.left = 0;
        min.right = 44 * Convert.MPH;
        max.bottom = -20 * Convert.MPH;
        min.top = 20 * Convert.MPH;
        
        x_major_units = 10 * Convert.MPH; 
        y_major_units = 10 * Convert.MPH;
    }

    @Override
    public void drawData(Canvas canvas) {
        long currentTime = System.currentTimeMillis();
        synchronized(MyLocationManager.history) {
            for(MyLocation loc : MyLocationManager.history) {
            	if(currentTime - window <= loc.timeMillis) {
	                double x = loc.groundSpeed();
	                double y = loc.climb;
	                // Style points
	                int t = (int) (currentTime - loc.timeMillis);
	                float radius = 16f * (6000 - t) / 8000;
	                radius = Math.max(3, Math.min(radius, 16));
	                int blue = 0xee * (20000 - t) / (20000 - 3000); // Fade color to dark blue
	                blue = Math.max(0x99, Math.min(blue, 0xff));
	                int alpha = 0xff * (60000 - t) / (60000 - 30000); // 0..1
	                alpha = Math.max(0, Math.min(alpha, 0xff));
	                int color = 0x01000000 * alpha + blue; // 0xff0000ee
	                drawPoint(canvas, x, y, radius, color);
            	}
            }
        }
    }
    
    // Always keep square aspect ratio
    private Bounds bounds = new Bounds();
    @Override
    public Bounds getBounds(int width, int height, Bounds dataBounds) {
        bounds.set(dataBounds);
        bounds.clean(min, max);
        bounds.squareBounds(width, height);
        return bounds;
    }

    @Override
    public String formatX(double x) {
        if(Math.abs(x) < EPSILON)
            return "";
        else
        	return Convert.speed(x, 0);
    }
    @Override
    public String formatY(double y) {
        if(Math.abs(y) < EPSILON)
            return "";
        else
        	return Convert.speed(y, 0);
    }

}

