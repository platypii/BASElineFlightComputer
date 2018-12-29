package com.platypii.baseline.views.charts;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.AdjustBounds;
import com.platypii.baseline.util.Bounds;
import com.platypii.baseline.util.DataSeries;
import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import java.util.List;

public class FlightProfile extends PlotView {

    private static final int AXIS_PROFILE = 0;
    private final Bounds bounds = new Bounds();

    private List<MLocation> trackData;
    private MLocation start;

    private final DataSeries profileSeries = new DataSeries();

    private final Bounds inner = new Bounds();
    private final Bounds outer = new Bounds();

    @Nullable
    private MLocation focus;

    public FlightProfile(Context context, AttributeSet attrs) {
        super(context, attrs);

        final float density = getResources().getDisplayMetrics().density;
        options.padding.top = (int) (12 * density);
        options.padding.bottom = (int) (4 * density);
        options.padding.left = (int) (density);
        options.padding.right = (int) (4 * density);

        inner.x.min = outer.x.min = 0;
        inner.x.max = 100;
        outer.x.max = 10000;
        inner.y.min = -100;
        outer.y.min = -10000;
        inner.y.max = 0;
        outer.y.max = 100;

        options.axis.x = options.axis.y = PlotOptions.axisDistance();

        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    public void loadTrack(@NonNull List<MLocation> trackData) {
        this.trackData = trackData;

        // Load track data into time series
        profileSeries.reset();
        if (!trackData.isEmpty()) {
            start = trackData.get(0);
            for (MLocation loc : trackData) {
                final double x = start.distanceTo(loc);
                final double y = loc.altitude_gps - start.altitude_gps;
                profileSeries.addPoint(x, y);
            }
        }
    }

    public void onFocus(@Nullable MLocation focus) {
        this.focus = focus;
        invalidate();
    }

    @Override
    public void drawData(@NonNull Plot plot) {
        if (trackData != null) {
            if (trackData.isEmpty()) {
                text.setTextAlign(Paint.Align.CENTER);
                plot.canvas.drawText("no track data", plot.width / 2, plot.height / 2, text);
            } else {
                // Draw data
                paint.setColor(0xff7f00ff);
                plot.drawLine(AXIS_PROFILE, profileSeries, 1.5f, paint);
            }
        }
        // Draw focus
        if (focus != null) {
            final double x = start.distanceTo(focus);
            final double y = focus.altitude_gps - start.altitude_gps;
            paint.setColor(0xcceeeeee);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(options.density);
            plot.drawPoint(0, x, y, 2 * options.density, paint);
            paint.setStyle(Paint.Style.FILL);
            plot.drawPoint(0, x, y, options.density, paint);
        }
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

}
