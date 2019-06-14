package com.platypii.baseline.tracks;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Range;
import androidx.annotation.NonNull;
import java.util.List;

/**
 * Compute track stats
 */
public class TrackStats {

    public final Range altitude = new Range();
    public MLocation exit;
    public MLocation land;

    TrackStats(@NonNull List<MLocation> trackData) {
        if (!trackData.isEmpty()) {
            // TODO: Detect exit and landing
            exit = trackData.get(0);
            land = trackData.get(trackData.size() - 1);
        }
        for (MLocation loc : trackData) {
            altitude.expand(loc.altitude_gps);
        }
    }

}
