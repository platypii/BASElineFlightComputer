package com.platypii.baseline.events;

import android.support.annotation.NonNull;

/**
 * Indicates that a track upload has completed, or sync status has changed
 */
public class SyncEvent {

    public static final int SYNC_UPLOAD_SUCCESS = 0;
    public static final int SYNC_UPLOAD_FAILED = 1;
    public static final int SYNC_LISTING = 2;

    // Sync event type
    public int type;

    // Optional error field
    public final String error;

    private SyncEvent(int type, String error) {
        this.type = type;
        this.error = error;
    }

    /**
     * Upload success event
     */
    public static SyncEvent success() {
        return new SyncEvent(SYNC_UPLOAD_SUCCESS, null);
    }

    /**
     * Upload error event
     */
    public static SyncEvent error(@NonNull String error) {
        return new SyncEvent(SYNC_UPLOAD_FAILED, error);
    }

    /**
     * Track listing updated event
     */
    public static SyncEvent listing() {
        return new SyncEvent(SYNC_LISTING, null);
    }

}
