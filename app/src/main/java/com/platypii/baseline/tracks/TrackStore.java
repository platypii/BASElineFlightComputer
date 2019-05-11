package com.platypii.baseline.tracks;

import com.platypii.baseline.BaseService;
import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.util.Exceptions;
import android.content.Context;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maintains the list of tracks
 */
public class TrackStore implements BaseService {
    private static final String TAG = "TrackStore";

    // Local track files
    private final Map<TrackFile,TrackState> trackState = new HashMap<>();
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
                    trackState.put(trackFile, new TrackState.TrackNotUploaded(trackFile));
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
            Log.e(TAG, "Track store not initialized");
        }
        final List<TrackFile> tracks = new ArrayList<>();
        for (TrackState state : trackState.values()) {
            if (state instanceof TrackState.TrackNotUploaded) {
                final TrackFile trackFile = ((TrackState.TrackNotUploaded) state).trackFile;
                tracks.add(trackFile);
            } else if (state instanceof TrackState.TrackUploading) {
                final TrackFile trackFile = ((TrackState.TrackUploading) state).trackFile;
                tracks.add(trackFile);
            }
        }
        // Sort by date descending
        Collections.sort(tracks, (track1, track2) -> -track1.getName().compareTo(track2.getName()));
        return tracks;
    }

    public void stop() {}

    void setRecording(@NonNull TrackFile trackFile) {
        trackState.put(trackFile, new TrackState.TrackRecording());
    }

    public void setNotUploaded(@NonNull TrackFile trackFile) {
        trackState.put(trackFile, new TrackState.TrackNotUploaded(trackFile));
    }

    public void setUploading(@NonNull TrackFile trackFile) {
        final TrackState state = trackState.get(trackFile);
        if (state instanceof TrackState.TrackNotUploaded) {
            trackState.put(trackFile, new TrackState.TrackUploading(trackFile));
        } else {
            Log.e(TAG, "Invalid track state transition: " + state + " -> uploading");
        }
    }

    public void setUploadSuccess(@NonNull TrackFile trackFile, @NonNull CloudData cloudData) {
        trackState.put(trackFile, new TrackState.TrackUploaded(cloudData));
    }

    public int getUploadProgress(@NonNull TrackFile trackFile) {
        final TrackState state = trackState.get(trackFile);
        if (state instanceof TrackState.TrackUploading) {
            return ((TrackState.TrackUploading) state).progress;
        } else {
            Log.e(TAG, "Invalid track state: cannot get upload progress in state " + state);
            return 0;
        }
    }

    public void setUploadProgress(@NonNull TrackFile trackFile, int bytesCopied) {
        final TrackState state = trackState.get(trackFile);
        if (state instanceof TrackState.TrackUploading) {
            ((TrackState.TrackUploading) state).progress = bytesCopied;
        } else {
            Log.e(TAG, "Invalid track state: upload progress in state " + state);
        }
    }

    public boolean isUploading(@NonNull TrackFile trackFile) {
        final TrackState state = trackState.get(trackFile);
        return state instanceof TrackState.TrackUploading;
    }

    @Nullable
    public CloudData getCloudData(@NonNull TrackFile trackFile) {
        final TrackState state = trackState.get(trackFile);
        if (state instanceof TrackState.TrackUploaded) {
            return ((TrackState.TrackUploaded) state).cloudData;
        } else {
            return null;
        }
    }

    /**
     * Delete local track file
     * @return true if track is deleted
     */
    public boolean delete(@NonNull TrackFile trackFile) {
        Log.w(TAG, "Deleting track file " + trackFile);
        if (!trackFile.file.exists()) {
            Exceptions.report(new FileNotFoundException("Trying to delete missing track file"));
            return true;
        }
        // Delete file on disk
        if (trackFile.file.delete()) {
            // Remove from store
            trackState.remove(trackFile);
            return true;
        } else {
            return false;
        }
    }

}
