package com.platypii.baseline.cloud.tasks;

import android.support.annotation.NonNull;

public abstract class TaskType {

    public abstract String name();
    public abstract Task fromJson(@NonNull String json);

}
