package com.platypii.baseline.cloud.lasers;

import com.platypii.baseline.cloud.tasks.Task;
import com.platypii.baseline.cloud.tasks.TaskType;
import com.platypii.baseline.laser.LaserProfile;
import androidx.annotation.NonNull;
import com.google.gson.Gson;

public class LaserUploadTaskType implements TaskType {

    @Override
    public String name() {
        return "LaserUpload";
    }

    /**
     * Parse from JSON into a LaserUploadTask
     */
    @Override
    public Task fromJson(@NonNull String json) {
        final LaserProfile laser = new Gson().fromJson(json, LaserProfile.class);
        return new LaserUploadTask(laser);
    }

    /**
     * Laser uploads are persisted, because they are not stores on disk.
     */
    @Override
    public boolean persistent() {
        return true;
    }

}
