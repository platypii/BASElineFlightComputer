package com.platypii.baseline.views.charts.layers;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.views.charts.Plot;
import android.graphics.Paint;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
    public void drawData(@NonNull Plot plot) {
        if (focus != null && start != null) {
            final double x = start.distanceTo(focus);
            final double y = focus.altitude_gps - start.altitude_gps;
            plot.paint.setColor(0xcceeeeee);
            plot.paint.setStyle(Paint.Style.STROKE);
            plot.paint.setStrokeJoin(Paint.Join.ROUND);
            plot.paint.setStrokeWidth(plot.options.density);
            plot.drawPoint(0, x, y, 2 * plot.options.density);
            plot.paint.setStyle(Paint.Style.FILL);
            plot.drawPoint(0, x, y, plot.options.density);
        }
    }

    public void onFocus(@Nullable MLocation focus) {
        this.focus = focus;
    }
}
