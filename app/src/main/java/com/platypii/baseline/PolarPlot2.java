package com.platypii.baseline;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import com.platypii.baseline.data.measurements.MLocation;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.util.SyncedList;

public class PolarPlot2 extends PolarPlot implements MyLocationListener {

    private static final int window = 30000; // The size of the view window, in milliseconds
    private final SyncedList<MLocation> history = new SyncedList<>();

    public PolarPlot2(Context context, AttributeSet attrs) {
        super(context, attrs);
        history.setMaxSize(300);
    }

    @Override
    public void drawData(Canvas canvas) {
        final long currentTime = System.currentTimeMillis() - Services.location.phoneOffsetMillis;
        synchronized(history) {
            for(MLocation loc : history) {
                final int t = (int) (currentTime - loc.millis);
                if(t <= window) {
                    final double x = loc.groundSpeed();
                    final double y = loc.climb;

                    // Style point based on freshness
                    final int purple = 0x5500ff;
                    int darkness = 0xff * (20000 - t) / (20000 - 1500); // Fade color to dark
                    darkness = Math.max(0x88, Math.min(darkness, 0xdd));
                    final int rgb = darken(purple, darkness);
                    int alpha = 0xff * (30000 - t) / (30000 - 10000); // 0..1
                    alpha = Math.max(0, Math.min(alpha, 0xff));
                    final int color = (alpha << 24) + rgb; // 0xff5500ff

                    // Draw point
                    float radius = 12f * (4000 - t) / 6000;
                    radius = Math.max(3, Math.min(radius, 16));
                    drawPoint(canvas, x, y, radius, color);
                }
            }
        }

        // Draw circle, line and point
        super.drawData(canvas);
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
