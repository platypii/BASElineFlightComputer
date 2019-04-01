package com.platypii.baseline.cloud.tasks;

import android.support.annotation.NonNull;

public class TaskTypes {

//    public static final TaskType trackUpload = new TrackUploadTaskType();

    @NonNull
    public static Task fromJson(@NonNull String name, @NonNull String json) {
//        if (name.equals(trackUpload.name())) {
//            return trackUpload.fromJson(json);
//        } else {
                throw new IllegalArgumentException("Unknown task type: " + name);
//        }
    }

}
