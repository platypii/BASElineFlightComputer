package com.platypii.baseline.cloud.tasks;

import androidx.annotation.NonNull;

public class TaskType {

    public static final TaskType laserUpload = new TaskType("LaserUpload");
    public static final TaskType trackUpload = new TaskType("TrackUpload");

    @NonNull
    private final String name;

    TaskType(@NonNull String name) {
        this.name = name;
    }

    /**
     * The name of this type of task
     */
    @NonNull
    String name() {
        return name;
    }
}
