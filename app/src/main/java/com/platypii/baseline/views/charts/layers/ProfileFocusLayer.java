package com.platypii.baseline.views.charts.layers;

import com.platypii.baseline.util.Numbers;
import com.platypii.baseline.views.charts.Plot;

import android.graphics.Paint;
import androidx.annotation.NonNull;

public class ProfileFocusLayer extends ChartLayer {

    private double x = Double.NaN;
    private double y = Double.NaN;

    @Override
    public void drawData(@NonNull Plot plot) {
        if (Numbers.isReal(x) && Numbers.isReal(y)) {
            plot.paint.setStyle(Paint.Style.FILL);
            plot.paint.setColor(0x88eeeeee);
            plot.drawPoint(0, x, y, 4 * plot.options.density);
            plot.paint.setColor(0xddeeeeee);
            plot.drawPoint(0, x, y, plot.options.density);
        }
    }

    public void onFocus(double x, double y) {
        this.x = x;
        this.y = y;
    }
}
