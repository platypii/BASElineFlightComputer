package com.platypii.baseline.cloud.tasks;

import android.content.Context;
import androidx.annotation.NonNull;

/**
 * Any background task
 */
public abstract class Task {

    /**
     * Unique id for the task.
     * Tasks are de-duped by id when added to Tasks service.
     */
    @NonNull
    public abstract String id();

    /**
     * Code to execute the task
     */
    public abstract void run(@NonNull Context context) throws Exception;

    @NonNull
    public abstract TaskType taskType();

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Task && ((Task) obj).id().equals(id());
    }

}
