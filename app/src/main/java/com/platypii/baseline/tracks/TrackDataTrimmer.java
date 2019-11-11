package com.platypii.baseline.tracks;

import com.platypii.baseline.measurements.MLocation;

import androidx.annotation.NonNull;
import java.util.List;

/**
 * Parse location data from track file
 */
class TrackDataTrimmer {

    /**
     * Trim plane ride and ground from track data
     */
    @NonNull
    static List<MLocation> autoTrim(@NonNull List<MLocation> points) {
        // Margin size is the number of data points on either side of the jump
        // TODO: Use time instead of samples
        final int margin_size = 50;
        final int n = points.size();
        // Scan data
        int index_start = 0;
        int index_end = n;
        for (int i = 0; i < n; i++) {
            final MLocation point = points.get(i);
            if (index_start == 0 && point.climb < -4) {
                index_start = i;
            }
            if (point.climb < -2.5 && index_start < i) {
                index_end = i;
            }
        }
        // Conform to list bounds
        index_start = index_start - margin_size < 0 ? 0 : index_start - margin_size;
        index_end = index_end + margin_size + 1 > n ? n : index_end + margin_size + 1;
        return points.subList(index_start, index_end);
    }

}
