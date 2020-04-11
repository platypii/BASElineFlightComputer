package com.platypii.baseline.views.charts;

import com.platypii.baseline.events.ChartFocusEvent;
import com.platypii.baseline.lasers.LaserMeasurement;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.AdjustBounds;
import com.platypii.baseline.util.Bounds;
import com.platypii.baseline.views.charts.layers.DiagonalLayer;
import com.platypii.baseline.views.charts.layers.ProfileFocusLayer;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;

public class FlightProfile extends PlotView {

    private final Bounds bounds = new Bounds();
    private final Bounds inner = new Bounds();
    private final Bounds outer = new Bounds();

    final ProfileFocusLayer focusLayer = new ProfileFocusLayer();

    public FlightProfile(Context context, AttributeSet attrs) {
        super(context, attrs);

        final float density = getResources().getDisplayMetrics().density;
        options.padding.top = (int) (18 * density);
        options.padding.bottom = (int) (4 * density);
        options.padding.left = (int) (density);
        options.padding.right = (int) (4 * density);

        inner.x.min = outer.x.min = 0;
        inner.x.max = 100;
        outer.x.max = 10000;
        inner.y.min = -61;
        outer.y.min = -8000;
        inner.y.max = 0;
        outer.y.max = 100;

        options.axis.x = options.axis.y = PlotOptions.axisDistance();

        addLayer(focusLayer);
        addLayer(new DiagonalLayer());
    }

    public void onChartFocus(@NonNull ChartFocusEvent focus) {
        if (focus instanceof ChartFocusEvent.LaserFocused) {
            final LaserMeasurement point = ((ChartFocusEvent.LaserFocused) focus).point;
            focusLayer.onFocus(point.x, point.y);
        } else if (focus instanceof ChartFocusEvent.TrackFocused) {
            final ChartFocusEvent.TrackFocused trackFocus = (ChartFocusEvent.TrackFocused) focus;
            if (!trackFocus.track.isEmpty()) {
                final MLocation start = trackFocus.track.get(0);
                final double x = start.distanceTo(trackFocus.location);
                final double y = trackFocus.location.altitude_gps - start.altitude_gps;
                focusLayer.onFocus(x, y);
            } else {
                focusLayer.onFocus(Double.NaN, Double.NaN);
            }
        } else {
            focusLayer.onFocus(Double.NaN, Double.NaN);
        }
        invalidate();
    }

    @Override
    public void drawData(@NonNull Plot plot) {
    }

    @NonNull
    @Override
    public Bounds getBounds(@NonNull Bounds dataBounds, int axis) {
        bounds.set(dataBounds);
        AdjustBounds.clean(bounds, inner, outer);
        AdjustBounds.squareBounds(bounds, getWidth(), getHeight(), options.padding);
        return bounds;
    }

}
