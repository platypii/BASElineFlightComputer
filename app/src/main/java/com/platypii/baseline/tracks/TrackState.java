package com.platypii.baseline.tracks;

import com.platypii.baseline.cloud.CloudData;

import androidx.annotation.NonNull;

/**
 * Manages track state: recording, not uploaded, uploading, uploaded
 */
class TrackState {

    static class TrackRecording extends TrackState {
    }

    static class TrackNotUploaded extends TrackState {
        @NonNull
        final TrackFile trackFile;

        TrackNotUploaded(@NonNull TrackFile trackFile) {
            this.trackFile = trackFile;
        }
    }

    static class TrackUploading extends TrackState {
        @NonNull
        final TrackFile trackFile;
        int progress = 0;

        TrackUploading(@NonNull TrackFile trackFile) {
            this.trackFile = trackFile;
        }
    }

    static class TrackUploaded extends TrackState {
        @NonNull
        final CloudData cloudData;

        TrackUploaded(@NonNull CloudData cloudData) {
            this.cloudData = cloudData;
        }
    }

}
