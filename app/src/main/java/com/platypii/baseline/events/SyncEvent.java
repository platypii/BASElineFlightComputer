package com.platypii.baseline.events;

import androidx.annotation.NonNull;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.tracks.TrackMetadata;
import java.io.File;

/**
 * Indicates that a track upload has completed, or sync status has changed
 */
public abstract class SyncEvent {

    public static abstract class DownloadEvent extends SyncEvent {
        public TrackMetadata track;
    }
    public static abstract class UploadEvent extends SyncEvent {
        public TrackFile trackFile;
    }
    public static abstract class DeleteEvent extends SyncEvent {
    }

    /* Downloads */
    public static class DownloadProgress extends DownloadEvent {
        public final int progress;
        public final int total;

        public DownloadProgress(@NonNull TrackMetadata track, int progress, int total) {
            this.track = track;
            this.progress = progress;
            this.total = total;
        }
    }

    public static class DownloadSuccess extends DownloadEvent {
        public final File trackFile;

        public DownloadSuccess(@NonNull TrackMetadata track, @NonNull File trackFile) {
            this.track = track;
            this.trackFile = trackFile;
        }
    }

    public static class DownloadFailure extends DownloadEvent {
        public final Exception error;

        public DownloadFailure(@NonNull TrackMetadata track, Exception error) {
            this.track = track;
            this.error = error;
        }
    }

    /* Uploads */
    public static class UploadProgress extends UploadEvent {
        public final int progress;

        public UploadProgress(@NonNull TrackFile trackFile, int progress) {
            this.trackFile = trackFile;
            this.progress = progress;
        }
    }

    public static class UploadSuccess extends UploadEvent {
        public final TrackMetadata cloudData;

        public UploadSuccess(@NonNull TrackFile trackFile, TrackMetadata cloudData) {
            this.trackFile = trackFile;
            this.cloudData = cloudData;
        }
    }

    public static class UploadFailure extends UploadEvent {
        public final String error;

        public UploadFailure(@NonNull TrackFile trackFile, String error) {
            this.trackFile = trackFile;
            this.error = error;
        }
    }

    /* Deletes */
    public static class DeleteSuccess extends DeleteEvent {
        public final String track_id;

        public DeleteSuccess(@NonNull String track_id) {
            this.track_id = track_id;
        }
    }

    public static class DeleteFailure extends DeleteEvent {
        public final String track_id;
        public final String error;

        public DeleteFailure(@NonNull String track_id, String error) {
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
