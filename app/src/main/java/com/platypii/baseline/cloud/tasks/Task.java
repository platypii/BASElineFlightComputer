package com.platypii.baseline.cloud.tasks;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Any background task
 */
public interface Task {

    /**
     * Code to execute the task
     */
    void run(@NonNull Context context) throws Exception;

    TaskType taskType();

    String toJson();

}
