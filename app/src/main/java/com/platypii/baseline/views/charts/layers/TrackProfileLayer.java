package com.platypii.baseline.views.charts.layers;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.tracks.TrackData;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;

public class TrackProfileLayer extends ProfileLayer {
    private final String name;

    public TrackProfileLayer(@NonNull String name, @NonNull TrackData trackData, @ColorInt int color) {
        super(color);
        this.name = name;

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

    @NonNull
    @Override
    public String id() {
        return name;
    }

    @NonNull
    @Override
    public String name() {
        return name;
    }

}
