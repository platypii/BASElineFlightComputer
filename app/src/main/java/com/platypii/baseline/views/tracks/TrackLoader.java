package com.platypii.baseline.views.tracks;

import com.platypii.baseline.Services;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.tracks.TrackMetadata;
import com.platypii.baseline.util.ABundle;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.io.File;

/**
 * Helpers for loading tracks between activities and fragments
 */
public class TrackLoader {

    public static final String EXTRA_TRACK_ID = "TRACK_ID";
    public static final String EXTRA_TRACK_FILE = "TRACK_FILE";

    @NonNull
    public static TrackMetadata loadCloudData(@Nullable Bundle extras) {
        // Load track id from extras
        if (extras != null) {
            final String track_id = extras.getString(EXTRA_TRACK_ID);
            if (track_id != null) {
                final TrackMetadata track = Services.tracks.cache.get(track_id);
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

    @NonNull
    static TrackFile loadTrackFile(@NonNull Activity activity) {
        return loadTrackFile(activity.getIntent().getExtras());
    }

    @NonNull
    public static TrackFile loadTrackFile(@NonNull Fragment fragment) {
        return loadTrackFile(fragment.getArguments());
    }

    @NonNull
    private static TrackFile loadTrackFile(@Nullable Bundle extras) {
        // Load track file from extras
        if (extras != null) {
            final String extraTrackFile = extras.getString(EXTRA_TRACK_FILE);
            if (extraTrackFile != null) {
                return new TrackFile(new File(extraTrackFile));
            } else {
                throw new IllegalStateException("Failed to load track file from extras");
            }
        } else {
            throw new IllegalStateException("Failed to load extras");
        }
    }

    @NonNull
    public static Bundle trackBundle(@NonNull TrackMetadata track) {
        return ABundle.of(EXTRA_TRACK_ID, track.track_id);
    }

    @NonNull
    public static Bundle trackBundle(@NonNull File file) {
        return ABundle.of(EXTRA_TRACK_FILE, file.getAbsolutePath());
    }

}
