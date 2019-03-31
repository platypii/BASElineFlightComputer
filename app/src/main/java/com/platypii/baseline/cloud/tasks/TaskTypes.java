package com.platypii.baseline.cloud.tasks;

import com.platypii.baseline.cloud.lasers.LaserUploadTaskType;
import com.platypii.baseline.cloud.tracks.TrackUploadTaskType;
import androidx.annotation.NonNull;

public class TaskTypes {

    public static final TaskType trackUpload = new TrackUploadTaskType();
    public static final TaskType laserUpload = new LaserUploadTaskType();

    @NonNull
    public static Task fromJson(@NonNull String name, @NonNull String json) {
        if (name.equals(trackUpload.name())) {
            return trackUpload.fromJson(json);
        } else if (name.equals(laserUpload.name())) {
            return laserUpload.fromJson(json);
        } else {
            throw new IllegalArgumentException("Unknown task type: " + name);
        }
    }

}
