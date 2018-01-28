package com.platypii.baseline.cloud;

import com.platypii.baseline.Services;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import java.util.List;

/**
 * Manage track data for uploaded tracks
 */
public class TrackDatabase {
    private static final String TAG = "TrackDatabase";

    /**
     * Return the most recent track data available
     */
    @Nullable
    public CloudData getCached(@NonNull String track_id) {
        final List<CloudData> tracks = Services.cloud.listing.cache.list();
        if(tracks != null) {
            for(CloudData track : tracks) {
                if(track.track_id.equals(track_id)) {
                    return track;
                }
            }
        }
        return null;
    }

    /**
     * Add track to list of synced tracks.
     * This is called when an upload succeeds, so that the track shows up immediately.
     */
    void addTrackData(@NonNull CloudData trackData) {
        // Update track list cache, if it exists
        final List<CloudData> trackList = Services.cloud.listing.cache.list();
        if(trackList != null) {
            trackList.add(trackData);
            Services.cloud.listing.cache.update(trackList);
        } else {
            // TODO: Save singleton track list?
            Log.e(TAG, "Failed to add to null track list");
        }
    }

}
