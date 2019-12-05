package com.platypii.baseline.views.charts.layers;

import com.platypii.baseline.views.charts.Plot;

import android.graphics.Paint;
import androidx.annotation.NonNull;

public class DiagonalLayer extends ChartLayer {

    @Override
    public void drawData(@NonNull Plot plot) {
        plot.paint.setStyle(Paint.Style.STROKE);
        plot.paint.setColor(plot.options.grid_color);
        plot.paint.setStrokeWidth(plot.options.density);
        plot.canvas.drawLine(plot.getX(0), plot.getY(0), plot.getX(10000), plot.getY(-10000), plot.paint);
    }

}
