package com.platypii.baseline.tracks;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Range;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.model.LatLngBounds;
import java.util.List;

/**
 * Compute track stats
 */
public class TrackStats {

    public final Range altitude = new Range();
    @Nullable
    public MLocation exit;
    @Nullable
    public MLocation deploy;
    @Nullable
    public MLocation land;

    @Nullable
    public final LatLngBounds bounds;

    TrackStats(@NonNull List<MLocation> trackData) {
        if (!trackData.isEmpty()) {
            // Detect exit, deploy, land
            final TrackLabels labels = TrackLabels.from(trackData);
            if (labels != null) {
                exit = trackData.get(labels.exit);
                deploy = trackData.get(labels.deploy);
                land = trackData.get(labels.land);
            }
            final LatLngBounds.Builder boundsBuilder = LatLngBounds.builder();
            for (MLocation loc : trackData) {
                altitude.expand(loc.altitude_gps);
                boundsBuilder.include(loc.latLng());
            }
            bounds = boundsBuilder.build();
        } else {
            bounds = null;
        }
    }

    public boolean isDefined() {
        return exit != null && deploy != null && land != null;
    }
}
