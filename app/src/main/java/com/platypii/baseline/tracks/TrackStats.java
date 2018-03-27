package com.platypii.baseline.tracks;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Range;
import android.support.annotation.NonNull;
import java.util.List;

/**
 * Compute track stats
 */
public class TrackStats {

    public final Range altitude = new Range();
    public MLocation exit;

    public TrackStats(@NonNull List<MLocation> trackData) {
        if (!trackData.isEmpty()) {
            exit = trackData.get(0);
        }
        for (MLocation loc : trackData) {
            altitude.expand(loc.altitude_gps);
        }
    }

}
