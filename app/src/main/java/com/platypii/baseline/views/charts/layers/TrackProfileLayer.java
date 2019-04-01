package com.platypii.baseline.views.charts.layers;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.tracks.TrackData;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

public class TrackProfileLayer extends ProfileLayer {

    public TrackProfileLayer(@NonNull String name, @NonNull TrackData trackData, @ColorInt int color) {
        super(color);
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
        this.name = name;
    }

}
