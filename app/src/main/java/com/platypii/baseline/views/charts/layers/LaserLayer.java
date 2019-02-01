package com.platypii.baseline.views.charts.layers;

import com.platypii.baseline.laser.LaserMeasurement;
import com.platypii.baseline.util.DataSeries;
import com.platypii.baseline.views.charts.Plot;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.List;

public class LaserLayer extends ChartLayer {

    private static final int AXIS_PROFILE = 0;
    private final DataSeries profileSeries = new DataSeries();
    @Nullable
    private List<LaserMeasurement> lasers;

    public void setPoints(List<LaserMeasurement> lasers) {
        this.lasers = lasers;
        // Load laser measurements into time series
        profileSeries.reset();
        profileSeries.addPoint(0, 0);
        if (!lasers.isEmpty()) {
            for (LaserMeasurement laser : lasers) {
                profileSeries.addPoint(laser.x, laser.y);
            }
        }
    }

    @Override
    public void drawData(@NonNull Plot plot) {
        if (lasers != null) {
            plot.paint.setColor(0xff7f00ff);
            plot.drawLine(AXIS_PROFILE, profileSeries, 1.5f);
        }
    }

}
