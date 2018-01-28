package com.platypii.baseline.events;

/**
 * Indicates that a track upload has completed, or sync status has changed
 */
public abstract class DownloadEvent {

    public static class DownloadProgress extends DownloadEvent {
        public final String track_id;
        public final int progress;
        public DownloadProgress(String track_id, int progress) {
            this.track_id = track_id;
            this.progress = progress;
        }
    }

    public static class DownloadSuccess extends DownloadEvent {
        public final String track_id;
        public DownloadSuccess(String track_id) {
            this.track_id = track_id;
        }
    }
    public static class DownloadFailure extends DownloadEvent {
        public final String track_id;
        public final String error;
        public DownloadFailure(String track_id, String error) {
            this.track_id = track_id;
            this.error = error;
        }
    }

}
