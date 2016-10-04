package com.platypii.baseline;

import com.platypii.baseline.data.Bounds;
import com.platypii.baseline.data.measurements.MLocation;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.SyncedList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

public class PolarPlot extends PlotView implements MyLocationListener {

    private static final long window = 30000; // The size of the view window, in milliseconds
    private final SyncedList<MLocation> history = new SyncedList<>();

    public PolarPlot(Context context, AttributeSet attrs) {
        super(context, attrs);

        final float density = getResources().getDisplayMetrics().density;
        padding.top = (int) (12 * density);
        padding.bottom = (int) (32 * density);
        padding.left = (int) (2 * density);
        padding.right = (int) (75 * density);
        
        min.left = max.left = 0;
        min.right = 5 * Convert.MPH;
        max.bottom = -2 * Convert.MPH;
        min.top = 2 * Convert.MPH;
        
        x_major_units = 1 * Convert.MPH;
        y_major_units = 1 * Convert.MPH;

        history.setMaxSize(300);
    }

    @Override
    public void drawData(Canvas canvas) {
        final long currentTime = System.currentTimeMillis() - Services.location.phoneOffsetMillis;
        final MLocation loc = Services.location.lastLoc;
        if(loc != null && currentTime - loc.millis <= window) {
            // Draw history
            drawHistory(canvas);

            // Draw current location
            drawLocation(canvas, loc);
        }
    }

    /**
     * Draw historical points
     */
    private void drawHistory(Canvas canvas) {
        final long currentTime = System.currentTimeMillis() - Services.location.phoneOffsetMillis;
        synchronized(history) {
            for(MLocation loc : history) {
                final int t = (int) (currentTime - loc.millis);
                if(t <= window) {
                    final double x = loc.groundSpeed();
                    final double y = loc.climb;

                    // Style point based on freshness
                    final int purple = 0x5500ff;
                    int darkness = 0xbb * (20000 - t) / (20000 - 1000); // Fade color to dark
                    darkness = Math.max(0x88, Math.min(darkness, 0xbb));
                    final int rgb = darken(purple, darkness);
                    int alpha = 0xff * (30000 - t) / (30000 - 10000); // 0..1
                    alpha = Math.max(0, Math.min(alpha, 0xff));
                    final int color = (alpha << 24) + rgb; // 0xff5500ff

                    // Draw point
                    float radius = 12f * (4000 - t) / 6000;
                    radius = Math.max(3, Math.min(radius, 12));
                    drawPoint(canvas, x, y, radius, color);
                }
            }
        }
    }

    /**
     * Draw the current location, including position, glide slope, and x and y axis ticks.
     */
    private void drawLocation(Canvas canvas, @NonNull MLocation loc) {
        final long currentTime = System.currentTimeMillis() - Services.location.phoneOffsetMillis;
        final double x = loc.groundSpeed();
        final double y = loc.climb;
        final double z = loc.totalSpeed();
        final float sx = getX(x);
        final float sy = getY(y);
        final float cx = getX(0);
        final float cy = getY(0);

        // Style point based on freshness
        final int t = (int) (currentTime - loc.millis);
        final int rgb = 0x5500ff;
        int alpha = 0xff * (30000 - t) / (30000 - 10000);
        alpha = Math.max(0, Math.min(alpha, 0xff));
        final int color = (alpha << 24) + rgb; // 0xff5500ff
        paint.setColor(color);

        // Draw line
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(2 * density);
        canvas.drawLine(cx, cy, sx, sy, paint);

        // Draw total speed circle
        final float r = Math.abs(getX(z) - cx);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(density);
        paint.setColor(0xdddddddd);
        canvas.drawCircle(cx, cy, r, paint);

        // Draw total speed label
        final String totalSpeed = Convert.speed(z);
        canvas.drawText(totalSpeed, sx + 6 * density, sy + 22 * density, text);
        final String glideRatio = Convert.glide(x, y, 2, true);
        canvas.drawText(glideRatio, sx + 6 * density, sy + 38 * density, text);

        // Draw axis ticks
        paint.setColor(0xffdddddd);
        paint.setStrokeCap(Paint.Cap.SQUARE);
        // paint.setStrokeWidth(density);
        drawXtick(canvas, x, Convert.speed(x));
        drawYtick(canvas, y, Convert.speed(Math.abs(y)));

        // Draw point
        float radius = 16f * (6000 - t) / 8000;
        radius = Math.max(3, Math.min(radius, 16));
        drawPoint(canvas, x, y, radius, color);
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

    /**
     * Darken a color by linear scaling of each color * (factor / 256)
     * @param color argb color 0xff5000be
     * @param factor scale factor out of 256
     * @return darkened color
     */
    private static int darken(int color, int factor) {
        final int a = (color >> 24) & 0xff;
        int r = (color >> 16) & 0xff;
        int g = (color >> 8) & 0xff;
        int b = (color) & 0xff;
        r = r * factor >> 8;
        g = g * factor >> 8;
        b = b * factor >> 8;
        return (a << 24) + (r << 16) + (g << 8) + b;
    }

//    @Override
//    public String formatX(double x) {
//        if(Math.abs(x) < EPSILON)
//            return "";
//        else
//            return Convert.speed(x, 0, true);
//    }
//    @Override
//    public String formatY(double y) {
//        final double y_abs = Math.abs(y);
//        if(y_abs < EPSILON)
//            return "";
//        else
//            return Convert.speed(y_abs, 0, true);
//    }

    public void start() {
        // Start listening for location updates
        Services.location.addListener(this);
    }
    public void stop() {
        // Stop listening for location updates
        Services.location.removeListener(this);
    }

    @Override
    public void onLocationChanged(MLocation loc) {
        history.append(loc);
    }
    @Override
    public void onLocationChangedPostExecute() {}

}