package com.platypii.baseline.views.charts.layers;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.DataSeries;
import com.platypii.baseline.views.charts.Plot;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import java.util.List;

public class ProfileLayer extends ChartLayer {

    private static final int AXIS_PROFILE = 0;
    private final List<MLocation> trackData;
    private final DataSeries profileSeries = new DataSeries();

    public ProfileLayer(@NonNull List<MLocation> trackData) {
        this.trackData = trackData;

        // Load track data into time series
        profileSeries.reset();
        if (!trackData.isEmpty()) {
            final MLocation start = trackData.get(0);
            for (MLocation loc : trackData) {
                final double x = start.distanceTo(loc);
                final double y = loc.altitude_gps - start.altitude_gps;
                profileSeries.addPoint(x, y);
            }
        }
    }

    @Override
    public void drawData(@NonNull Plot plot, Paint paint, Paint text) {
        if (trackData != null) {
            if (trackData.isEmpty()) {
                text.setTextAlign(Paint.Align.CENTER);
                plot.canvas.drawText("no track data", plot.width / 2, plot.height / 2, text);
            } else {
                // Draw data
                paint.setColor(0xff7f00ff);
                plot.drawLine(AXIS_PROFILE, profileSeries, 1.5f, paint);
            }
        }
    }

}
