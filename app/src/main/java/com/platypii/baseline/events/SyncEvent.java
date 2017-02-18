package com.platypii.baseline.events;

/**
 * Indicates that a track upload has completed, or sync status has changed
 */
public abstract class SyncEvent {

    public static class UploadSuccess extends SyncEvent {}
    public static class UploadFailure extends SyncEvent {
        public final String error;
        public UploadFailure(String error) {
            this.error = error;
        }
    }

    public static class DeleteSuccess extends SyncEvent {
        public final String track_id;
        DeleteSuccess(String track_id) {
            this.track_id = track_id;
        }
    }
    public static class DeleteFailure extends SyncEvent {
        public final String track_id;
        public final String error;
        DeleteFailure(String track_id, String error) {
            this.track_id = track_id;
            this.error = error;
        }
    }

    /**
     * Track listing updated event
     */
    public static class ListingSuccess extends SyncEvent {}

}
