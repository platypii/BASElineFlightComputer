package com.platypii.baseline.views.tracks;

import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.CloudData;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class TrackLoader {

    public static final String EXTRA_TRACK_ID = "TRACK_ID";

    @NonNull
    public static CloudData loadTrack(@Nullable Bundle extras) {
        // Load track id from extras
        if (extras != null) {
            final String track_id = extras.getString(EXTRA_TRACK_ID);
            if (track_id != null) {
                final CloudData track = Services.cloud.listing.cache.getTrack(track_id);
                if (track != null) {
                    return track;
                } else {
                    throw new IllegalStateException("Failed to load track from track_id " + track_id);
                }
            } else {
                throw new IllegalStateException("Failed to load track_id from extras");
            }
        } else {
            throw new IllegalStateException("Failed to load extras");
        }
    }

}
