package com.platypii.baseline.tracks;

import com.platypii.baseline.measurements.MLocation;

import android.support.annotation.NonNull;
import java.io.File;
import java.util.List;

/**
 * Parse location data from track file
 */
public class TrackData {

    @NonNull
    public final List<MLocation> data;

    @NonNull
    public final TrackStats stats;

    public TrackData(@NonNull File trackFile) {
        final List<MLocation> all = TrackFileReader.readTrackFile(trackFile);
        // Trim plane and ground
        data = TrackDataTrimmer.autoTrim(all);
        // Compute stats
        stats = new TrackStats(data);
    }

}
