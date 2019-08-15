package com.platypii.baseline.views.charts.layers;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.DataSeries;
import com.platypii.baseline.views.charts.Plot;
import androidx.annotation.NonNull;
import java.util.List;

public class SpeedDataLayer extends ChartLayer {

    private static final int AXIS_SPEED = 0;
    private final DataSeries dataSeries = new DataSeries();

    public SpeedDataLayer(@NonNull List<MLocation> trackData) {
        // Load track data into time series
        for (MLocation loc : trackData) {
            dataSeries.addPoint(loc.groundSpeed(), loc.climb);
        }
    }

    @Override
    public void drawData(@NonNull Plot plot) {
        plot.paint.setColor(Colors.defaultColor);
        plot.drawLine(AXIS_SPEED, dataSeries, 1.5f);
    }
}
