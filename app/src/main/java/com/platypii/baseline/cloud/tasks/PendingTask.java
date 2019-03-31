package com.platypii.baseline.cloud.tasks;

import androidx.annotation.NonNull;

public class PendingTask {
    @NonNull
    public String name;
    @NonNull
    public String json;

    PendingTask(@NonNull String name, @NonNull String json) {
        this.name = name;
        this.json = json;
    }
}
