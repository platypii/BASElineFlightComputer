package com.platypii.baseline.events;

/**
 * Indicates that laser profile data may have updated
 */
public abstract class LaserSyncEvent {

    public static class UploadSuccess extends LaserSyncEvent {}
    public static class UploadFailure extends LaserSyncEvent {}

}
