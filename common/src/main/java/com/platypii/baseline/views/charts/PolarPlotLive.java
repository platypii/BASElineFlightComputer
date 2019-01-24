package com.platypii.baseline.views.charts;

import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.location.LocationProvider;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.location.TimeOffset;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.AdjustBounds;
import com.platypii.baseline.util.Bounds;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.SyncedList;
import com.platypii.baseline.views.charts.layers.EllipseLayer;
import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;

public class PolarPlotLive extends PlotSurface implements MyLocationListener {

    private static final int AXIS_POLAR = 0;
    private final EllipseLayer ellipses;
    private final Bounds inner = new Bounds();
    private final Bounds outer = new Bounds();
    private final Bounds bounds = new Bounds();

    private static final long window = 15000; // The size of the view window, in milliseconds
    private final SyncedList<MLocation> history = new SyncedList<>();

    private LocationProvider locationService = null;
    private MyAltimeter altimeter = null;

    public PolarPlotLive(Context context, AttributeSet attrs) {
        super(context, attrs);

        final float density = getResources().getDisplayMetrics().density;
        options.padding.top = (int) (12 * density);
        options.padding.bottom = (int) (42 * density);
        options.padding.left = (int) (density);
        options.padding.right = (int) (76 * density);

        inner.x.min = outer.x.min = 0;
        inner.x.max = 9 * Convert.MPH;
        outer.x.max = 160 * Convert.MPH;
        inner.y.min = -2 * Convert.MPH;
        outer.y.min = -160 * Convert.MPH;
        inner.y.max = 2 * Convert.MPH;
        outer.y.max = 28 * Convert.MPH;

        options.axis.x = options.axis.y = PlotOptions.axisSpeed();

        history.setMaxSize(300);

        // Add layers
        ellipses = new EllipseLayer(options.density);
        ellipses.setEnabled(false); // Disable until the first data comes in
        addLayer(ellipses);
    }

    @Override
    public void drawData(@NonNull Plot plot) {
        if (locationService != null) {
            final long currentTime = TimeOffset.phoneToGpsTime(System.currentTimeMillis());
            final MLocation loc = locationService.lastLoc;
            if (loc != null && currentTime - loc.millis <= window) {
                ellipses.setEnabled(true);

                // Draw horizontal, vertical speed
                final double vx = locationService.groundSpeed();
                final double vy = altimeter.climb;
                drawSpeedLines(plot, vx, vy);

                // Draw history
                drawHistory(plot);

                // Draw horizontal, vertical speed labels
                drawSpeedLabels(plot, vx, vy);

                // Draw current location
                drawLocation(plot, loc.millis, vx, vy);
            } else {
                // Draw "no gps signal"
                ellipses.setEnabled(false);
                text.setTextAlign(Paint.Align.CENTER);
                plot.canvas.drawText("no gps signal", plot.width / 2, plot.height / 2, text);
            }
        }
    }

    /**
     * Draw historical points
     */
    private void drawHistory(@NonNull Plot plot) {
        final long currentTime = TimeOffset.phoneToGpsTime(System.currentTimeMillis());
        synchronized (history) {
            paint.setStyle(Paint.Style.FILL);
            for (MLocation loc : history) {
                final int t = (int) (currentTime - loc.millis);
                if (t <= window) {
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
                    paint.setColor(color);
                    plot.drawPoint(AXIS_POLAR, vx, vy, radius, paint);
                }
            }
        }
    }

    /**
     * Draw the current location, including position, glide slope, and x and y axis ticks.
     */
    private void drawLocation(@NonNull Plot plot, long millis, double vx, double vy) {
        // Style point based on freshness
        final long currentTime = TimeOffset.phoneToGpsTime(System.currentTimeMillis());
        final int t = (int) (currentTime - millis);
        final int rgb = 0x5500ff;
        int alpha = 0xff * (30000 - t) / (30000 - 10000);
        alpha = Math.max(0, Math.min(alpha, 0xff));
        final int color = (alpha << 24) + rgb; // 0xff5500ff

        // Draw point
        float radius = 16f * (6000 - t) / 8000;
        radius = Math.max(3, Math.min(radius, 16));
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        plot.drawPoint(AXIS_POLAR, vx, vy, radius, paint);
    }

    private void drawSpeedLines(@NonNull Plot plot, double vx, double vy) {
        final double v = Math.sqrt(vx*vx + vy*vy);
        final float sx = plot.getX(vx);
        final float sy = plot.getY(vy);
        final float cx = plot.getX(0);
        final float cy = plot.getY(0);

        // Draw horizontal and vertical speed lines
        paint.setStrokeWidth(options.density);
        paint.setColor(0xff666666);
        plot.canvas.drawLine(cx, (int) sy, sx, (int) sy, paint); // Horizontal
        plot.canvas.drawLine((int) sx, cy, (int) sx, sy, paint); // Vertical

        // Draw total speed circle
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(0xff444444);
        final float r = Math.abs(plot.getX(v) - cx);
        plot.canvas.drawCircle(cx, cy, r, paint);

        // Draw glide line
        paint.setColor(0xff999999);
        paint.setStrokeCap(Paint.Cap.ROUND);
        plot.canvas.drawLine(cx, cy, sx, sy, paint);
    }

    private void drawSpeedLabels(@NonNull Plot plot, double vx, double vy) {
        final double v = Math.sqrt(vx*vx + vy*vy);
        final float sx = plot.getX(vx);
        final float sy = plot.getY(vy);
        final float cx = plot.getX(0);
        final float cy = plot.getY(0);

        // Draw horizontal and vertical speed labels (unless near axis)
        text.setColor(0xff888888);
        if (sy - cy < -44 * options.density || 18 * options.density < sy - cy) {
            // Horizontal speed label
            plot.canvas.drawText(Convert.speed(vx, 0, true), sx + 3 * options.density, cy + 16 * options.density, text);
        }
        if (42 * options.density < sx - cx) {
            // Vertical speed label
            plot.canvas.drawText(Convert.speed(Math.abs(vy), 0, true), cx + 3 * options.density, sy + 16 * options.density, text);
        }

        // Draw total speed label
        text.setColor(0xffcccccc);
        text.setTextAlign(Paint.Align.LEFT);
        final String totalSpeed = Convert.speed(v);
        plot.canvas.drawText(totalSpeed, sx + 6 * options.density, sy + 22 * options.density, text);
        final String glideRatio = Convert.glide2(vx, vy, 2, true);
        plot.canvas.drawText(glideRatio, sx + 6 * options.density, sy + 40 * options.density, text);
    }

    // Always keep square aspect ratio
    @NonNull
    @Override
    public Bounds getBounds(@NonNull Bounds dataBounds, int axis) {
        bounds.set(dataBounds);
        AdjustBounds.clean(bounds, inner, outer);
        AdjustBounds.squareBounds(bounds, getWidth(), getHeight(), options.padding);
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

    public void start(@NonNull LocationProvider locationService, @NonNull MyAltimeter altimeter) {
        this.locationService = locationService;
        this.altimeter = altimeter;
        // Start listening for location updates
        locationService.addListener(this);
    }
    public void stop() {
        // Stop listening for location updates
        locationService.removeListener(this);
    }

    @Override
    public void onLocationChanged(@NonNull MLocation loc) {
        history.append(loc);
    }

}
