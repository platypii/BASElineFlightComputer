package com.platypii.baseline.events;

import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.tracks.TrackMetadata;

/**
 * Indicates that a track upload has completed, or sync status has changed
 */
public abstract class SyncEvent {

    public static class UploadProgress extends SyncEvent {
        public final TrackFile trackFile;
        public final int progress;

        public UploadProgress(TrackFile trackFile, int progress) {
            this.trackFile = trackFile;
            this.progress = progress;
        }
    }

    public static class UploadSuccess extends SyncEvent {
        public final TrackFile trackFile;
        public final TrackMetadata cloudData;

        public UploadSuccess(TrackFile trackFile, TrackMetadata cloudData) {
            this.trackFile = trackFile;
            this.cloudData = cloudData;
        }
    }

    public static class UploadFailure extends SyncEvent {
        public final TrackFile trackFile;
        public final String error;

        public UploadFailure(TrackFile trackFile, String error) {
            this.trackFile = trackFile;
            this.error = error;
        }
    }

    public static class DeleteSuccess extends SyncEvent {
        public final String track_id;

        public DeleteSuccess(String track_id) {
            this.track_id = track_id;
        }
    }

    public static class DeleteFailure extends SyncEvent {
        public final String track_id;
        public final String error;

        public DeleteFailure(String track_id, String error) {
            this.track_id = track_id;
            this.error = error;
        }
    }

    /**
     * Track listing updated event
     */
    public static class ListingSuccess extends SyncEvent {
    }

}
