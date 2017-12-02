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

    private final Map<TrackFile,Integer> trackFileState = new HashMap<>();

    public int getState(@NonNull TrackFile trackFile) {
        if(trackFileState.containsKey(trackFile)) {
            return trackFileState.get(trackFile);
        } else {
            return NOT_UPLOADED;
        }
    }

    public void setState(@NonNull TrackFile trackFile, int state) {
        trackFileState.put(trackFile, state);
    }

}
