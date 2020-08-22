package com.platypii.baseline.views.charts.layers;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.util.DataSeries;
import com.platypii.baseline.views.charts.Plot;

import android.graphics.Paint;
import androidx.annotation.NonNull;

public class SpeedDataLayer extends ChartLayer {

    private static final int AXIS_SPEED = 0;
    private final DataSeries plane = new DataSeries();
    private final DataSeries flight = new DataSeries();
    private final DataSeries canopy = new DataSeries();
    private final DataSeries ground = new DataSeries();

    public SpeedDataLayer(@NonNull TrackData trackData) {
        // Load track data into time series
        for (MLocation loc : trackData.data) {
            if (loc.millis <= trackData.stats.exit.millis) {
                plane.addPoint(loc.groundSpeed(), loc.climb);
            }
            if (trackData.stats.exit.millis <= loc.millis && loc.millis <= trackData.stats.deploy.millis) {
                flight.addPoint(loc.groundSpeed(), loc.climb);
            }
            if (trackData.stats.deploy.millis <= loc.millis && loc.millis <= trackData.stats.land.millis) {
                canopy.addPoint(loc.groundSpeed(), loc.climb);
            }
            if (trackData.stats.land.millis <= loc.millis) {
                ground.addPoint(loc.groundSpeed(), loc.climb);
            }
        }
    }

    @Override
    public void drawData(@NonNull Plot plot) {
        plot.paint.setStrokeCap(Paint.Cap.ROUND);
        plot.paint.setColor(Colors.modeGround);
        plot.drawLine(AXIS_SPEED, ground, 1.2f);
        plot.paint.setColor(Colors.modePlane);
        plot.drawLine(AXIS_SPEED, plane, 1.2f);
        plot.paint.setColor(Colors.modeCanopy);
        plot.drawLine(AXIS_SPEED, canopy, 1.2f);
        plot.paint.setColor(Colors.modeWingsuit);
        plot.drawLine(AXIS_SPEED, flight, 1.2f);
    }
}
