package com.platypii.baseline.tracks;

import com.platypii.baseline.Services;
import com.platypii.baseline.tracks.LocalTrackState.TrackNotUploaded;
import com.platypii.baseline.tracks.LocalTrackState.TrackUploaded;
import com.platypii.baseline.tracks.LocalTrackState.TrackUploading;
import com.platypii.baseline.tracks.LocalTrackState.TrackRecording;
import com.platypii.baseline.util.Exceptions;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maintains the list of tracks and their sync state
 */
public class LocalTracks {
    private static final String TAG = "LocalTracks";

    // Local track files
    private final Map<TrackFile, LocalTrackState> trackState = new HashMap<>();
    private boolean initialized = false;

    public void start(@NonNull Context context) {
        if (!initialized) {
            initialized = true;
            // Load in background
            AsyncTask.execute(() -> {
                // Get log directory
                final File logDir = TrackFiles.getTrackDirectory(context);
                // Load from disk
                final List<TrackFile> trackFiles = TrackFiles.getTracks(logDir);
                for (TrackFile trackFile : trackFiles) {
                    trackState.put(trackFile, new TrackNotUploaded(trackFile));
                }
            });
        }
    }

    /**
     * Return non-uploaded and non-recording tracks.
     * In other words, the things we should list in track listing view.
     */
    @NonNull
    public List<TrackFile> getLocalTracks() {
        if (!initialized) {
            Exceptions.report(new IllegalStateException("TrackStore not initialized before getLocalTracks()"));
        }
        final List<TrackFile> tracks = new ArrayList<>();
        for (LocalTrackState state : trackState.values()) {
            if (state instanceof TrackNotUploaded) {
                final TrackFile trackFile = ((TrackNotUploaded) state).trackFile;
                tracks.add(trackFile);
            } else if (state instanceof TrackUploading) {
                final TrackFile trackFile = ((TrackUploading) state).trackFile;
                tracks.add(trackFile);
            }
        }
        // Sort by date descending
        Collections.sort(tracks, (track1, track2) -> -track1.getName().compareTo(track2.getName()));
        return tracks;
    }

    void setRecording(@NonNull TrackFile trackFile) {
        trackState.put(trackFile, new TrackRecording());
    }

    void setNotUploaded(@NonNull TrackFile trackFile) {
        trackState.put(trackFile, new TrackNotUploaded(trackFile));
    }

    public void setUploading(@NonNull TrackFile trackFile) {
        final LocalTrackState state = trackState.get(trackFile);
        if (state instanceof TrackNotUploaded) {
            trackState.put(trackFile, new TrackUploading(trackFile));
        } else {
            Log.e(TAG, "Invalid track state transition: " + state + " -> uploading");
        }
    }

    void setUploadSuccess(@NonNull TrackFile trackFile, @NonNull TrackMetadata cloudData) {
        trackState.put(trackFile, new TrackUploaded(cloudData));
    }

    public int getUploadProgress(@NonNull TrackFile trackFile) {
        final LocalTrackState state = trackState.get(trackFile);
        if (state instanceof TrackUploading) {
            return ((TrackUploading) state).progress;
        } else {
            Log.e(TAG, "Invalid track state: cannot get upload progress in state " + state);
            return 0;
        }
    }

    void setUploadProgress(@NonNull TrackFile trackFile, int bytesCopied) {
        final LocalTrackState state = trackState.get(trackFile);
        if (state instanceof TrackUploading) {
            ((TrackUploading) state).progress = bytesCopied;
        } else {
            Log.e(TAG, "Invalid track state: upload progress in state " + state);
        }
    }

    public boolean isUploading(@NonNull TrackFile trackFile) {
        final LocalTrackState state = trackState.get(trackFile);
        return state instanceof TrackUploading;
    }

    @Nullable
    public TrackMetadata getCloudData(@NonNull TrackFile trackFile) {
        final LocalTrackState state = trackState.get(trackFile);
        if (state instanceof TrackUploaded) {
            return ((TrackUploaded) state).cloudData;
        } else {
            return null;
        }
    }

    /**
     * Delete local track file
     *
     * @return true if track is deleted
     */
    public boolean delete(@NonNull TrackFile trackFile) {
        Log.w(TAG, "Deleting track file " + trackFile);
        if (isUploading(trackFile)) {
            Exceptions.report(new IllegalStateException("Failed to delete, upload in progress"));
            return false;
        }
        if (!trackFile.file.exists()) {
            Exceptions.report(new FileNotFoundException("Trying to delete missing track file"));
            return true;
        }
        // Delete file on disk
        if (trackFile.file.delete()) {
            // Remove from store
            trackState.remove(trackFile);
            // Reload local tracks
            Services.tracks.sync.uploadAll();
            return true;
        } else {
            return false;
        }
    }

}
