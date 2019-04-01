package com.platypii.baseline.cloud.tracks;

import com.platypii.baseline.cloud.tasks.Task;
import com.platypii.baseline.cloud.tasks.TaskType;
import com.platypii.baseline.tracks.TrackFile;
import android.support.annotation.NonNull;
import com.google.gson.Gson;
import java.io.File;

public class TrackUploadTaskType implements TaskType {

    @Override
    public String name() {
        return "TrackUpload";
    }

    /**
     * Parse from JSON into a TrackUploadTask
     */
    @Override
    public Task fromJson(@NonNull String json) {
        final String trackFileName = new Gson().fromJson(json, String.class);
        final TrackFile trackFile = new TrackFile(new File(trackFileName));
        return new TrackUploadTask(trackFile);
    }

    /**
     * Track uploads are not persisted, because they get reloaded from track files.
     */
    @Override
    public boolean persistent() {
        return false;
    }

}
