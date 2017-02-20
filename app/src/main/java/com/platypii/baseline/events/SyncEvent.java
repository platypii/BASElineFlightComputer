package com.platypii.baseline.events;

import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.tracks.TrackFile;

/**
 * Indicates that a track upload has completed, or sync status has changed
 */
public abstract class SyncEvent {

    public static class UploadSuccess extends SyncEvent {
        public final TrackFile trackFile;
        public final TrackData trackData;
        public UploadSuccess(TrackFile trackFile, TrackData trackData) {
            this.trackFile = trackFile;
            this.trackData = trackData;
        }
    }
    public static class UploadFailure extends SyncEvent {
        public final String error;
        public UploadFailure(String error) {
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
    public static class ListingSuccess extends SyncEvent {}

}
