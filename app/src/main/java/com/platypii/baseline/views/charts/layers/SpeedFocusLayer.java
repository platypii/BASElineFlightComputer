package com.platypii.baseline.views.charts.layers;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.views.charts.Plot;

import android.graphics.Paint;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SpeedFocusLayer extends ChartLayer {

    @Nullable
    private MLocation focus;

    @Override
    public void drawData(@NonNull Plot plot) {
        if (focus != null) {
            final double x = focus.groundSpeed();
            final double y = focus.climb;
            plot.paint.setStyle(Paint.Style.FILL);
            plot.paint.setColor(0x997f00ff);
            plot.drawPoint(0, x, y, 4 * plot.options.density);
            plot.paint.setColor(0xddeeeeee);
            plot.drawPoint(0, x, y, plot.options.density);
        }
    }

    public void onFocus(@Nullable MLocation focus) {
        this.focus = focus;
    }
}
