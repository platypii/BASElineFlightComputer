package com.platypii.baseline.events;

import android.support.annotation.NonNull;

/**
 * Indicates that a track upload has completed, or sync status has changed
 */
public class SyncEvent {

    public static final int TYPE_UPLOAD = 0;
    private static final int TYPE_LISTING = 1;

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
        return new SyncEvent(TYPE_UPLOAD, null);
    }

    /**
     * Upload error event
     */
    public static SyncEvent error(@NonNull String error) {
        return new SyncEvent(TYPE_UPLOAD, error);
    }

    /**
     * Track listing updated event
     */
    public static SyncEvent listing() {
        return new SyncEvent(TYPE_LISTING, null);
    }

}
