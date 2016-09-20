package com.platypii.baseline;

import com.platypii.baseline.data.Bounds;
import com.platypii.baseline.data.DataSeries;
import com.platypii.baseline.data.measurements.MLocation;
import com.platypii.baseline.util.Convert;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

public class PolarPlot extends PlotView {

    private static final long window = 30000; // The size of the view window, in milliseconds
    private final DataSeries series = new DataSeries();

    public PolarPlot(Context context, AttributeSet attrs) {
        super(context, attrs);

        final float density = getResources().getDisplayMetrics().density;
        padding.top = (int) (16 * density);
        padding.bottom = (int) (30 * density);
        padding.left = (int) (2 * density);
        padding.right = (int) (30 * density);
        
        min.left = max.left = 0;
        min.right = 20 * Convert.MPH;
        max.bottom = -8 * Convert.MPH;
        min.top = 8 * Convert.MPH;
        
        x_major_units = 10 * Convert.MPH;
        y_major_units = 10 * Convert.MPH;
    }

    @Override
    public void drawData(Canvas canvas) {
        final long currentTime = System.currentTimeMillis() - Services.location.phoneOffsetMillis;
        final MLocation loc = Services.location.lastLoc;
        if(loc != null && currentTime - loc.millis <= window) {
            final double x = loc.groundSpeed();
            final double y = loc.climb;

            // Style point based on freshness
            final int t = (int) (currentTime - loc.millis);
            final int rgb = 0x5500ff;
            int alpha = 0xff * (30000 - t) / (30000 - 10000);
            alpha = Math.max(0, Math.min(alpha, 0xff));
            final int color = 0x01000000 * alpha + rgb; // 0xff5500ff
            paint.setColor(color);

            // Draw line
            series.reset();
            series.addPoint(0,0);
            series.addPoint(x,y);
            paint.setStrokeCap(Paint.Cap.ROUND);
            drawLine(canvas, series, 1);

            // Draw total speed circle
            final float r = Math.abs(getX(loc.totalSpeed()) - getX(0));
            paint.setStrokeWidth(1.5f);
            paint.setColor(0xffdddddd);
            canvas.drawCircle(getX(0), getY(0), r, paint);

            // Draw point
            float radius = 16f * (6000 - t) / 8000;
            radius = Math.max(3, Math.min(radius, 16));
            drawPoint(canvas, x, y, radius, color);
        }
    }
    
    // Always keep square aspect ratio
    private Bounds bounds = new Bounds();
    @Override
    public Bounds getBounds(Bounds dataBounds) {
//        final long currentTime = System.currentTimeMillis();
//        final MLocation loc = Services.location.lastLoc;
//        if(loc != null && currentTime - window <= loc.millis) {
//            // Fixed left boundary at ground speed = 0
//            final double l = 0;
//            // Right boundary is at least 80mph
//            double r = Math.max(80 * Convert.MPH, loc.groundSpeed());
//            // Determine top/bottom bounds
//            double b = -1;
//            double t = 1;
//            if(loc.climb > t) {
//                t = Math.max(t, loc.climb);
//            } else {
//                b = Math.min(b, loc.climb);
//            }
//            bounds.set(l, t, r, b);
//            bounds.squareBounds(getWidth(), getHeight());
//        } else {
        bounds.set(dataBounds);
        bounds.clean(min, max);
        bounds.squareBounds(getWidth(), getHeight(), padding);
        return bounds;
    }

    @Override
    public String formatX(double x) {
        if(Math.abs(x) < EPSILON)
            return "";
        else
            return Convert.speed(x, 0, true);
    }
    @Override
    public String formatY(double y) {
        final double y_abs = Math.abs(y);
        if(y_abs < EPSILON)
            return "";
        else
            return Convert.speed(y_abs, 0, true);
    }

}
