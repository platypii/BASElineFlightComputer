package com.platypii.baseline.views.charts.layers;

import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.views.charts.Plot;

import android.graphics.Paint;
import androidx.annotation.NonNull;

public class FlightModeLayer extends ChartLayer {

    @NonNull
    private final TrackData trackData;

    public FlightModeLayer(@NonNull TrackData trackData) {
        this.trackData = trackData;
    }

    @Override
    public void drawData(@NonNull Plot plot) {
        if (trackData.stats.isDefined()) {
            final int w = plot.width;
            final int h = plot.height;
            final float thic = 4 * plot.options.density;
            final float x1 = plot.getX(trackData.stats.exit.millis);
            final float x2 = plot.getX(trackData.stats.deploy.millis);
            final float x3 = plot.getX(trackData.stats.land.millis);
            plot.paint.setStyle(Paint.Style.FILL);
            plot.paint.setColor(Colors.modeGround);
            plot.canvas.drawRect(0, h - thic, x1, h, plot.paint);
            plot.paint.setColor(Colors.modeWingsuit);
            plot.canvas.drawRect(x1, h - thic, x2, h, plot.paint);
            plot.paint.setColor(Colors.modeCanopy);
            plot.canvas.drawRect(x2, h - thic, x3, h, plot.paint);
            plot.paint.setColor(Colors.modeGround);
            plot.canvas.drawRect(x3, h - thic, w, h, plot.paint);
        }
    }
}
