package com.platypii.baseline.views.charts;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.util.AdjustBounds;
import com.platypii.baseline.util.Bounds;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.views.charts.layers.EllipseLayer;
import com.platypii.baseline.views.charts.layers.SpeedDataLayer;
import com.platypii.baseline.views.charts.layers.SpeedFocusLayer;
import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SpeedChart extends PlotView {

    @Nullable
    TrackData trackData;

    private final Bounds bounds = new Bounds();
    private final Bounds inner = new Bounds();
    private final Bounds outer = new Bounds();

    private final SpeedFocusLayer focusLayer = new SpeedFocusLayer();

    public SpeedChart(Context context, AttributeSet attrs) {
        super(context, attrs);

        final float density = getResources().getDisplayMetrics().density;
        options.padding.top = (int) (18 * density);
        options.padding.bottom = (int) (4 * density);
        options.padding.left = (int) (density);
        options.padding.right = (int) (4 * density);

        inner.x.min = outer.x.min = 0;
        inner.x.max = 9 * Convert.MPH;
        outer.x.max = 160 * Convert.MPH;
        inner.y.min = -2 * Convert.MPH;
        outer.y.min = -160 * Convert.MPH;
        inner.y.max = 0;
        outer.y.max = 28 * Convert.MPH;

        options.axis.x = options.axis.y = PlotOptions.axisSpeed();
    }

    public void loadTrack(@NonNull TrackData trackData) {
        this.trackData = trackData;
        // Add layers
        if (!trackData.data.isEmpty()) {
            addLayer(new EllipseLayer(options.density));
        }
        addLayer(new SpeedDataLayer(trackData));
        addLayer(focusLayer);
    }

    public void onFocus(@Nullable MLocation focus) {
        focusLayer.onFocus(focus);
        invalidate();
    }

    @Override
    public void drawData(@NonNull Plot plot) {}

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
