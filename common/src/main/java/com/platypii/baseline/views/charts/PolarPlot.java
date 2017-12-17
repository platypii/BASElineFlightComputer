package com.platypii.baseline.views.charts;

import com.platypii.baseline.location.LocationProvider;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.location.TimeOffset;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Bounds;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.SyncedList;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

public class PolarPlot extends PlotView implements MyLocationListener {

    private static final long window = 15000; // The size of the view window, in milliseconds
    private final SyncedList<MLocation> history = new SyncedList<>();

    private LocationProvider locationService = null;

    public PolarPlot(Context context, AttributeSet attrs) {
        super(context, attrs);

        final float density = getResources().getDisplayMetrics().density;
        padding.top = (int) (12 * density);
        padding.bottom = (int) (42 * density);
        padding.left = (int) (density);
        padding.right = (int) (76 * density);
        
        min.left = max.left = 0;
        min.right = 9 * Convert.MPH;
        max.right = 160 * Convert.MPH;
        min.bottom = -160 * Convert.MPH;
        max.bottom = -2 * Convert.MPH;
        min.top = 2 * Convert.MPH;
        max.top = 28 * Convert.MPH;
        
        x_major_units = y_major_units = Convert.metric? Convert.KPH : Convert.MPH;

        history.setMaxSize(300);
    }

    @Override
    public void drawData(@NonNull Canvas canvas) {
        if(locationService != null) {
            final long currentTime = System.currentTimeMillis() - TimeOffset.phoneOffsetMillis;
            final MLocation loc = locationService.lastLoc;
            if(loc != null && currentTime - loc.millis <= window) {
                // Draw background ellipses
                drawEllipses(canvas);

                // Draw horizontal, vertical speed
                final double vx = loc.groundSpeed();
                final double vy = loc.climb;
                drawSpeedLines(canvas, vx, vy);

                // Draw history
                drawHistory(canvas);

                // Draw horizontal, vertical speed labels
                drawSpeedLabels(canvas, vx, vy);

                // Draw current location
                drawLocation(canvas, loc.millis, vx, vy);
            } else {
                // Draw "no gps signal"
                text.setTextAlign(Paint.Align.CENTER);
                canvas.drawText("no gps signal", (left + right) / 2, (top + bottom) / 2, text);
            }
        }
    }

    private final BlurMaskFilter blurry = new BlurMaskFilter(2 * density, BlurMaskFilter.Blur.NORMAL);
    private void drawEllipses(@NonNull Canvas canvas) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            paint.setStyle(Paint.Style.FILL);
            paint.setMaskFilter(blurry);
            // Draw canopy ellipse
            paint.setColor(0x2244ee44);
            canvas.save();
            canvas.rotate(9, getX(11), getY(-5.5));
            canvas.drawOval(getX(1), getY(-1), getX(21), getY(-10), paint);
            canvas.restore();
            // Draw wingsuit ellipse
            paint.setColor(0x229e62f2);
            canvas.save();
            canvas.rotate(35, getX(38), getY(-21));
            canvas.drawOval(getX(20), getY(-10), getX(56), getY(-32), paint);
            canvas.restore();
            paint.setMaskFilter(null);
        }
    }

    /**
     * Draw historical points
     */
    private void drawHistory(@NonNull Canvas canvas) {
        final long currentTime = System.currentTimeMillis() - TimeOffset.phoneOffsetMillis;
        synchronized(history) {
            for(MLocation loc : history) {
                final int t = (int) (currentTime - loc.millis);
                if(t <= window) {
                    final double vx = loc.groundSpeed();
                    final double vy = loc.climb;

                    // Style point based on freshness
                    final int purple = 0x5500ff;
                    int darkness = 0xbb * (15000 - t) / (15000 - 1000); // Fade color to dark
                    darkness = Math.max(0x88, Math.min(darkness, 0xbb));
                    final int rgb = darken(purple, darkness);
                    int alpha = 0xff * (15000 - t) / (15000 - 10000); // fade out at t=10..15
                    alpha = Math.max(0, Math.min(alpha, 0xff));
                    final int color = (alpha << 24) + rgb; // 0xff5500ff

                    // Draw point
                    float radius = 12f * (4000 - t) / 6000;
                    radius = Math.max(3, Math.min(radius, 12));
                    drawPoint(canvas, vx, vy, radius, color);
                }
            }
        }
    }

    /**
     * Draw the current location, including position, glide slope, and x and y axis ticks.
     */
    private void drawLocation(@NonNull Canvas canvas, long millis, double vx, double vy) {
        // Style point based on freshness
        final long currentTime = System.currentTimeMillis() - TimeOffset.phoneOffsetMillis;
        final int t = (int) (currentTime - millis);
        final int rgb = 0x5500ff;
        int alpha = 0xff * (30000 - t) / (30000 - 10000);
        alpha = Math.max(0, Math.min(alpha, 0xff));
        final int color = (alpha << 24) + rgb; // 0xff5500ff

        // Draw point
        float radius = 16f * (6000 - t) / 8000;
        radius = Math.max(3, Math.min(radius, 16));
        drawPoint(canvas, vx, vy, radius, color);
    }

    private void drawSpeedLines(@NonNull Canvas canvas, double vx, double vy) {
        final double v = Math.sqrt(vx*vx + vy*vy);
        final float sx = getX(vx);
        final float sy = getY(vy);
        final float cx = getX(0);
        final float cy = getY(0);

        // Draw horizontal and vertical speed lines
        paint.setStrokeWidth(density);
        paint.setColor(0xff666666);
        canvas.drawLine(cx, (int) sy, sx, (int) sy, paint); // Horizontal
        canvas.drawLine((int) sx, cy, (int) sx, sy, paint); // Vertical

        // Draw total speed circle
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xff444444);
        final float r = Math.abs(getX(v) - cx);
        canvas.drawCircle(cx, cy, r, paint);

        // Draw glide line
        paint.setColor(0xff999999);
        paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawLine(cx, cy, sx, sy, paint);
    }

    private void drawSpeedLabels(@NonNull Canvas canvas, double vx, double vy) {
        final double v = Math.sqrt(vx*vx + vy*vy);
        final float sx = getX(vx);
        final float sy = getY(vy);
        final float cx = getX(0);
        final float cy = getY(0);

        // Draw horizontal and vertical speed labels (unless near axis)
        text.setColor(0xff888888);
        if(sy - cy < -44 * density || 18 * density < sy - cy) {
            // Horizontal speed label
            canvas.drawText(Convert.speed(vx, 0, true), sx + 3 * density, cy + 16 * density, text);
        }
        if(42 * density < sx - cx) {
            // Vertical speed label
            canvas.drawText(Convert.speed(Math.abs(vy), 0, true), cx + 3 * density, sy + 16 * density, text);
        }

        // Draw total speed label
        text.setColor(0xffcccccc);
        text.setTextAlign(Paint.Align.LEFT);
        final String totalSpeed = Convert.speed(v);
        canvas.drawText(totalSpeed, sx + 6 * density, sy + 22 * density, text);
        final String glideRatio = Convert.glide2(vx, vy, 2, true);
        canvas.drawText(glideRatio, sx + 6 * density, sy + 40 * density, text);
    }

    // Always keep square aspect ratio
    private final Bounds bounds = new Bounds();
    @NonNull
    @Override
    public Bounds getBounds(@NonNull Bounds dataBounds) {
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

    public void start(@NonNull LocationProvider locationService) {
        this.locationService = locationService;
        // Start listening for location updates
        locationService.addListener(this);
    }
    public void stop() {
        // Stop listening for location updates
        locationService.removeListener(this);
        locationService = null;
    }

    @Override
    public void onLocationChanged(@NonNull MLocation loc) {
        history.append(loc);
    }
    @Override
    public void onLocationChangedPostExecute() {}

}
