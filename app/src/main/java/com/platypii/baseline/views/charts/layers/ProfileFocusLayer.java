package com.platypii.baseline.views.charts.layers;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.views.charts.Plot;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.List;

public class ProfileFocusLayer extends ChartLayer {

    @Nullable
    private final MLocation start;

    @Nullable
    private MLocation focus;

    public ProfileFocusLayer(@NonNull List<MLocation> trackData) {
        // Load start point
        if (!trackData.isEmpty()) {
            start = trackData.get(0);
        } else {
            start = null;
        }
    }

    @Override
    public void drawData(@NonNull Plot plot, Paint paint, Paint text) {
        if (focus != null && start != null) {
            final double x = start.distanceTo(focus);
            final double y = focus.altitude_gps - start.altitude_gps;
            paint.setColor(0xcceeeeee);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(plot.options.density);
            plot.drawPoint(0, x, y, 2 * plot.options.density, paint);
            paint.setStyle(Paint.Style.FILL);
            plot.drawPoint(0, x, y, plot.options.density, paint);
        }
    }

    public void onFocus(@Nullable MLocation focus) {
        this.focus = focus;
    }
}
