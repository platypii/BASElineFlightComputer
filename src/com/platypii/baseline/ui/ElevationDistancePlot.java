package com.platypii.baseline.ui;

import com.platypii.baseline.data.Bounds;
import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyLocation;
import com.platypii.baseline.data.MyLocationManager;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;


public class ElevationDistancePlot extends PlotView {

    private DataSeries series = new DataSeries();
    private PlotMode mode = PlotMode.LINE;
    
    
    public ElevationDistancePlot(Context context, AttributeSet attrs) {
        super(context, attrs);

        final float density = context.getResources().getDisplayMetrics().density;
        padding_top = (int) (14 * density);
        padding_bottom = (int) (6 * density);
        padding_left = (int) (2 * density);
        padding_right = (int) (2 * density);
        
        min.left = 0;
        max.bottom = 0;
        min.top = 200 * Convert.FT;
        
        x_major_units = (float) Convert.FT;
        y_major_units = (float) Convert.FT;
    }

    @Override
    public void drawData(Canvas canvas) {
        series.reset();
        float groundDistance = MyLocationManager.groundDistance;
        double altitude = MyAltimeter.altitude;
        // Copy values to local history (so that we don't block while drawing circles)
        synchronized(MyLocationManager.history) {
            for(MyLocation loc : MyLocationManager.history) {
            	double x = loc.groundDistance;
                double y = loc.altitude;
                series.addPoint(x, y);
            }
            series.addPoint(groundDistance, altitude);
        }
        // Draw data
        paint.setStrokeCap(Cap.ROUND);
        paint.setStrokeJoin(Join.ROUND);
        paint.setColor(0xff1144dd);
        if(mode == PlotMode.DOT) {
            drawPoints(canvas, series, 4, 0xff0000ff);
        } else if(mode == PlotMode.LINE) {
            drawLine(canvas, series, 3);
        } else if(mode == PlotMode.AREA) {
            paint.setShader(new LinearGradient(0, getY(0), 0, (2 * top + bottom) / 3, 0xff1144dd, 0xff5599bb, TileMode.CLAMP));
            drawArea(canvas, series, 0, 2);
            paint.setShader(null);
        }
        // Draw most recent point
        drawPoint(canvas, groundDistance, altitude, 6, 0xff1122ff);
    }
    
    private Bounds bounds = new Bounds();
    @Override
    public Bounds getBounds(int width, int height, Bounds dataBounds) {
        // Always show +/- 1000ft
        float currentDist = MyLocationManager.groundDistance;
        bounds.set(currentDist - 1, dataBounds.top, currentDist + 1, dataBounds.bottom);
        bounds.clean(min, max);
        bounds.squareBounds(width, height);
        return bounds;
    }
    
    @Override
    public String formatX(double x) {
        if(Math.abs(x) < EPSILON)
            return "";
        else
            return Convert.distance(x);
    }
    @Override
    public String formatY(double y) {
        if(Math.abs(y) < EPSILON)
            return "";
        else
            return Convert.distance(y);
    }

}

