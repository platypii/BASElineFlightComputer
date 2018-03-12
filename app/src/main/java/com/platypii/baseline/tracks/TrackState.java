package com.platypii.baseline.tracks;

import com.platypii.baseline.cloud.CloudData;
import android.support.annotation.NonNull;

/**
 * Manages track state: recording, not uploaded, uploading, uploaded
 */
class TrackState {

    static class TrackRecording extends TrackState {}
    static class TrackNotUploaded extends TrackState {
        final TrackFile trackFile;

        TrackNotUploaded(@NonNull TrackFile trackFile) {
            this.trackFile = trackFile;
        }
    }
    static class TrackUploading extends TrackState {
        final TrackFile trackFile;
        int progress = 0;

        TrackUploading(@NonNull TrackFile trackFile) {
            this.trackFile = trackFile;
        }
    }
    static class TrackUploaded extends TrackState {
        final CloudData cloudData;

        TrackUploaded(@NonNull CloudData cloudData) {
            this.cloudData = cloudData;
        }
    }

}
