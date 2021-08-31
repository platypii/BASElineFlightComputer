package com.platypii.baseline.tracks;

import com.platypii.baseline.measurements.MLocation;

import androidx.annotation.NonNull;
import java.io.File;
import java.util.List;

/**
 * Parse location data from track file
 */
public class TrackData {

    @NonNull
    private final String name;

    @NonNull
    public final List<MLocation> data;

    @NonNull
    public final TrackStats stats;

    public TrackData(@NonNull File trackFile) {
        name = trackFile.getName();
        final List<MLocation> all = new TrackFileReader(trackFile).read();
        // Trim plane and ground
        data = TrackDataTrimmer.autoTrim(all);
        // Compute stats
        stats = new TrackStats(data);
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

}
