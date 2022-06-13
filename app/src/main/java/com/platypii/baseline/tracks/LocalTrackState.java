package com.platypii.baseline.tracks;

import androidx.annotation.NonNull;

/**
 * Manages track state: recording, not uploaded, uploading, uploaded
 */
class LocalTrackState {

    static class TrackRecording extends LocalTrackState {
    }

    static class TrackNotUploaded extends LocalTrackState {
        @NonNull
        final TrackFile trackFile;

        TrackNotUploaded(@NonNull TrackFile trackFile) {
            this.trackFile = trackFile;
        }
    }

    static class TrackUploading extends LocalTrackState {
        @NonNull
        final TrackFile trackFile;
        int progress = 0;

        TrackUploading(@NonNull TrackFile trackFile) {
            this.trackFile = trackFile;
        }
    }

    static class TrackUploaded extends LocalTrackState {
        @NonNull
        final TrackMetadata cloudData;

        TrackUploaded(@NonNull TrackMetadata cloudData) {
            this.cloudData = cloudData;
        }
    }

}
