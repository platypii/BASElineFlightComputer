package com.platypii.baseline.views.charts.layers;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.util.DataSeries;
import com.platypii.baseline.views.charts.Plot;
import android.graphics.Paint;
import android.support.annotation.NonNull;

public class ProfileLayer extends ChartLayer {

    private static final int AXIS_PROFILE = 0;
    private final DataSeries profileSeries = new DataSeries();

    public void loadTrack(@NonNull TrackData trackData) {
        // Load track data into time series
        profileSeries.reset();
        if (!trackData.data.isEmpty()) {
            final MLocation start = trackData.data.get(0);
            for (MLocation loc : trackData.data) {
                final double x = start.distanceTo(loc);
                final double y = loc.altitude_gps - start.altitude_gps;
                profileSeries.addPoint(x, y);
            }
        }
    }

    @Override
    public void drawData(@NonNull Plot plot) {
        if (profileSeries.size() == 0) {
            plot.text.setTextAlign(Paint.Align.CENTER);
            plot.canvas.drawText("no track data", plot.width / 2, plot.height / 2, plot.text);
        } else {
            // Draw data
            plot.paint.setColor(0xff7f00ff);
            plot.drawLine(AXIS_PROFILE, profileSeries, 1.5f);
        }
    }

}
