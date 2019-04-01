package com.platypii.baseline.cloud.tasks;

import android.support.annotation.NonNull;

public interface TaskType {

    /**
     * The name of this type of task
     */
    String name();

    /**
     * Parse from JSON into a Task of this type
     */
    Task fromJson(@NonNull String json);

    /**
     * Return true if this task should be persisted across start/stops
     */
    boolean persistent();


}
