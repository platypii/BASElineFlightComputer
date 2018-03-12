package com.platypii.baseline.tracks;

import com.platypii.baseline.BaseService;
import com.platypii.baseline.cloud.CloudData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Maintains the list of tracks
 */
public class TrackStore implements BaseService {
    private static final String TAG = "TrackStore";

    // Local track files
    private final Map<String,TrackState> trackState = new HashMap<>();
    private boolean initialized = false;

    public void start(@NonNull Context context) {
        if (!initialized) {
            initialized = true;
            // Load from disk
            final List<TrackFile> trackFiles = TrackFiles.getTracks(context);
            for (TrackFile trackFile : trackFiles) {
                trackState.put(trackFile.getName(), new TrackState.TrackNotUploaded(trackFile));
            }
        } else {
            // TODO: Reload? But don't lose uploading state
        }
    }

    /**
     * Return non-uploaded and non-recording tracks.
     * In other words, the things we should list in track listing view.
     */
    public List<TrackFile> getLocalTracks() {
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
        return tracks;
    }

    public void stop() {}

    void setRecording(@NonNull TrackFile trackFile) {
        final String filename = trackFile.file.getName();
        trackState.put(filename, new TrackState.TrackRecording());
    }

    public void setNotUploaded(@NonNull TrackFile trackFile) {
        final String filename = trackFile.file.getName();
        trackState.put(filename, new TrackState.TrackNotUploaded(trackFile));
    }

    public void setUploading(@NonNull TrackFile trackFile) {
        final String filename = trackFile.file.getName();
        final TrackState state = trackState.get(filename);
        if (state != null && state instanceof TrackState.TrackNotUploaded) {
            trackState.put(filename, new TrackState.TrackUploading(trackFile));
        } else {
            Log.e(TAG, "Invalid track state transition: " + state + " -> uploading");
        }
    }

    public void setUploadSuccess(@NonNull TrackFile trackFile, @NonNull CloudData cloudData) {
        final String filename = trackFile.file.getName();
        trackState.put(filename, new TrackState.TrackUploaded(cloudData));
    }

    public int getUploadProgress(@NonNull TrackFile trackFile) {
        final String filename = trackFile.file.getName();
        final TrackState state = trackState.get(filename);
        if (state != null && state instanceof TrackState.TrackUploading) {
            return ((TrackState.TrackUploading) state).progress;
        } else {
            Log.e(TAG, "Invalid track state: cannot get upload progress in state " + state);
            return 0;
        }
    }

    public void setUploadProgress(@NonNull TrackFile trackFile, int bytesCopied) {
        final String filename = trackFile.file.getName();
        final TrackState state = trackState.get(filename);
        if (state != null && state instanceof TrackState.TrackUploading) {
            ((TrackState.TrackUploading) state).progress = bytesCopied;
        } else {
            Log.e(TAG, "Invalid track state: upload progress in state " + state);
        }
    }

    public boolean isUploading(@NonNull TrackFile trackFile) {
        final String filename = trackFile.file.getName();
        final TrackState state = trackState.get(filename);
        return state != null && state instanceof TrackState.TrackUploading;
    }

    @Nullable
    public CloudData getCloudData(@NonNull TrackFile trackFile) {
        final String filename = trackFile.file.getName();
        final TrackState state = trackState.get(filename);
        if (state != null && state instanceof TrackState.TrackUploaded) {
            return ((TrackState.TrackUploaded) state).cloudData;
        } else {
            return null;
        }
    }

}
