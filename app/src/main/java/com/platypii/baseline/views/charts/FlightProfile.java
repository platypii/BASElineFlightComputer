package com.platypii.baseline.views.charts;

import com.platypii.baseline.laser.LaserMeasurement;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.util.AdjustBounds;
import com.platypii.baseline.util.Bounds;
import com.platypii.baseline.views.charts.layers.Colors;
import com.platypii.baseline.views.charts.layers.LaserLayer;
import com.platypii.baseline.views.charts.layers.ProfileFocusLayer;
import com.platypii.baseline.views.charts.layers.TrackProfileLayer;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import java.util.List;

public class FlightProfile extends PlotView {

    List<MLocation> trackData;

    private final Bounds bounds = new Bounds();
    private final Bounds inner = new Bounds();
    private final Bounds outer = new Bounds();

    private final LaserLayer laserLayer = new LaserLayer();
    private ProfileFocusLayer focusLayer;

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
        inner.y.min = -61;
        outer.y.min = -8000;
        inner.y.max = 0;
        outer.y.max = 100;

        options.axis.x = options.axis.y = PlotOptions.axisDistance();

        // Add layers
        addLayer(laserLayer);
    }

    public void loadTrack(@NonNull TrackData trackData) {
        this.trackData = trackData.data;

        final TrackProfileLayer trackLayer = new TrackProfileLayer("", trackData, Colors.defaultColor);
        addLayer(trackLayer);
        focusLayer = new ProfileFocusLayer(trackData.data);
        addLayer(focusLayer);
    }

    public void setLasers(@NonNull List<LaserMeasurement> lasers) {
        laserLayer.setPoints(lasers);
        invalidate();
    }

    public void onFocus(@Nullable MLocation focus) {
        focusLayer.onFocus(focus);
        invalidate();
    }

    @Override
    public void drawData(@NonNull Plot plot) {}

    @NonNull
    @Override
    public Bounds getBounds(@NonNull Bounds dataBounds, int axis) {
        bounds.set(dataBounds);
        AdjustBounds.clean(bounds, inner, outer);
        AdjustBounds.squareBounds(bounds, getWidth(), getHeight(), options.padding);
        return bounds;
    }

}
