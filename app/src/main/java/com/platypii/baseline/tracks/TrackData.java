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
        data = Trimmer.autoTrim(all);
        // Compute stats
        stats = new TrackStats(data);
    }

    private TrackData(@NonNull String name, @NonNull List<MLocation> data, @NonNull TrackStats stats) {
        this.name = name;
        this.data = data;
        this.stats = stats;
    }

    /**
     * Return a new TrackData with trimmed data
     */
    public TrackData trim(long start, long end) {
        int startIndex = 0;
        int endIndex = 0;
        for (int i = 0; i < data.size(); i++) {
            final MLocation loc = data.get(i);
            if (loc.millis < start) startIndex = i;
            if (loc.millis <= end) endIndex = i;
        }
        final List<MLocation> trimmed = data.subList(startIndex, endIndex);
        return new TrackData(name, trimmed, stats);
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

}
