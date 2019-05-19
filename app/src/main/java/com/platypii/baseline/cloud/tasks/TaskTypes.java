package com.platypii.baseline.cloud.tasks;

import com.platypii.baseline.cloud.lasers.LaserUploadTaskType;
import com.platypii.baseline.cloud.tracks.TrackUploadTaskType;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class TaskTypes {

    public static final TaskType trackUpload = new TrackUploadTaskType();
    public static final TaskType laserUpload = new LaserUploadTaskType();

    static final List<TaskType> all = new ArrayList<>();

    static {
        all.add(trackUpload);
        all.add(laserUpload);
    }

    @NonNull
    static Task fromJson(@NonNull String name, @NonNull String json) {
        for (TaskType taskType : all) {
            if (name.equals(taskType.name())) {
                return taskType.fromJson(json);
            }
        }
        throw new IllegalArgumentException("Unknown task type: " + name);
    }

}
