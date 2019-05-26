package com.platypii.baseline.cloud.tasks;

import androidx.annotation.NonNull;

public abstract class TaskType {

    public static final TaskType laserUpload = new LaserTaskType();
    public static final TaskType trackUpload = new TrackTaskType();

    /**
     * The name of this type of task
     */
    @NonNull
    abstract String name();

    public static class LaserTaskType extends TaskType {
        @NonNull
        @Override
        String name() {
            return "LaserUpload";
        }
    }

    public static class TrackTaskType extends TaskType {
        @NonNull
        @Override
        String name() {
            return "TrackUpload";
        }
    }

}
