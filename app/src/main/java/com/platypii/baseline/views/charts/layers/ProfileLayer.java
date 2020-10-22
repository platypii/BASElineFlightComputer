package com.platypii.baseline.views.charts.layers;

import com.platypii.baseline.util.DataSeries;
import com.platypii.baseline.views.charts.Plot;

import android.graphics.Paint;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

public abstract class ProfileLayer extends ChartLayer {

    private static final int AXIS_DISTANCE = 0;
    protected final DataSeries dataSeries = new DataSeries();

    @ColorInt
    public final int color;
    public final float strokeWidth;

    protected ProfileLayer(@ColorInt int color) {
        this(color, 1.2f);
    }

    protected ProfileLayer(@ColorInt int color, float strokeWidth) {
        this.color = color;
        this.strokeWidth = strokeWidth;
    }

    @NonNull
    public abstract String id();

    @NonNull
    public abstract String name();

    @Override
    public void drawData(@NonNull Plot plot) {
        plot.paint.setColor(color);
        plot.paint.setStrokeCap(Paint.Cap.ROUND);
        plot.drawLine(AXIS_DISTANCE, dataSeries, strokeWidth);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ProfileLayer && ((ProfileLayer) obj).id().equals(id());
    }

    @NonNull
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "(" + id() + ", " + name() + ")";
    }

}
