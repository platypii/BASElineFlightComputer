package com.platypii.baseline.events;

import com.platypii.baseline.laser.LaserProfile;

import androidx.annotation.NonNull;

/**
 * Indicates that a laser upload, list or delete has completed
 */
public abstract class LaserSyncEvent {

    public static class UploadSuccess extends LaserSyncEvent {
        public final LaserProfile laserProfile;
        public UploadSuccess(LaserProfile laserProfile) {
            this.laserProfile = laserProfile;
        }
    }
    public static class UploadFailure extends LaserSyncEvent {
        public final LaserProfile laserProfile;
        public final String error;
        public UploadFailure(LaserProfile laserProfile, String error) {
            this.laserProfile = laserProfile;
            this.error = error;
        }
    }

    public static class DeleteSuccess extends LaserSyncEvent {
        @NonNull
        public final LaserProfile laserProfile;
        public DeleteSuccess(@NonNull LaserProfile laserProfile) {
            this.laserProfile = laserProfile;
        }
    }
    public static class DeleteFailure extends LaserSyncEvent {
        public final String laser_id;
        public final String error;
        public DeleteFailure(String laser_id, String error) {
            this.laser_id = laser_id;
            this.error = error;
        }
    }

    /**
     * Laser listing updated event
     */
    public static class ListingSuccess extends LaserSyncEvent {}

}
