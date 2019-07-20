package com.platypii.baseline.events;

import java.io.File;

/**
 * Indicates that a track upload has completed, or sync status has changed
 */
public abstract class DownloadEvent {

    public static class DownloadProgress extends DownloadEvent {
        public final String track_id;
        public final int progress;
        public final int total;
        public DownloadProgress(String track_id, int progress, int total) {
            this.track_id = track_id;
            this.progress = progress;
            this.total = total;
        }
    }

    public static class DownloadSuccess extends DownloadEvent {
        public final String track_id;
        public final File trackFile;
        public DownloadSuccess(String track_id, File trackFile) {
            this.track_id = track_id;
            this.trackFile = trackFile;
        }
    }
    public static class DownloadFailure extends DownloadEvent {
        public final String track_id;
        public final Exception error;
        public final boolean networkAvailable;
        public DownloadFailure(String track_id, Exception error, boolean networkAvailable) {
            this.track_id = track_id;
            this.error = error;
            this.networkAvailable = networkAvailable;
        }
    }

}
