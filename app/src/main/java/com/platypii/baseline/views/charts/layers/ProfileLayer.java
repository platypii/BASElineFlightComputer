package com.platypii.baseline.views.charts.layers;

import com.platypii.baseline.util.DataSeries;
import com.platypii.baseline.views.charts.Plot;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

public abstract class ProfileLayer extends ChartLayer {

    private static final int AXIS_PROFILE = 0;
    protected final DataSeries profileSeries = new DataSeries();
    @NonNull
    public String id;
    @NonNull
    public String name;
    @ColorInt
    public final int color;

    ProfileLayer(@NonNull String id, @NonNull String name, @ColorInt int color) {
        this.color = color;
        this.id = id;
        this.name = name;
    }

    @Override
    public void drawData(@NonNull Plot plot) {
        plot.paint.setColor(color);
        plot.drawLine(AXIS_PROFILE, profileSeries, 1.5f);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ProfileLayer && ((ProfileLayer) obj).id.equals(id);
    }

    @NonNull
    @Override
    public String toString() {
        return "ProfileLayer(" + id + ", " + name + ")";
    }

}
