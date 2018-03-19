package com.platypii.baseline.tracks;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Range;
import java.util.List;

/**
 * Compute track stats
 */
public class TrackStats {

    public Range altitude = new Range();
    public MLocation exit;

    public TrackStats(List<MLocation> trackData) {
        exit = trackData.get(0);
        for (MLocation loc : trackData) {
            altitude.expand(loc.altitude_gps);
        }
    }

}
