package com.platypii.baseline.tracks;

import android.support.annotation.NonNull;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages track state: recording, not uploaded, uploading, uploaded
 */
public class TrackState {

    // Upload state for each track file
    public static final int NOT_UPLOADED = 0;
    public static final int RECORDING = 1;
    public static final int UPLOADING = 2;
    public static final int UPLOADED = 3;

    private final Map<TrackFile,Integer> syncState = new HashMap<>();

    // Upload progress per track file
    private final Map<TrackFile,Integer> uploadProgress = new HashMap<>();

    public int getState(@NonNull TrackFile trackFile) {
        if(syncState.containsKey(trackFile)) {
            return syncState.get(trackFile);
        } else {
            return NOT_UPLOADED;
        }
    }

    public void setState(@NonNull TrackFile trackFile, int state) {
        syncState.put(trackFile, state);
    }

    public int getUploadProgress(@NonNull TrackFile trackFile) {
        final Integer progress = uploadProgress.get(trackFile);
        if(progress != null) return uploadProgress.get(trackFile);
        else return 0;
    }

    public void setUploadProgress(@NonNull TrackFile trackFile, int progress) {
        uploadProgress.put(trackFile, progress);
    }

}
