package com.platypii.baseline.cloud.tracks;

import com.platypii.baseline.cloud.UploadFailedException;
import com.platypii.baseline.cloud.UploadTask;
import com.platypii.baseline.cloud.tasks.AuthRequiredException;
import com.platypii.baseline.cloud.tasks.Task;
import com.platypii.baseline.cloud.tasks.TaskType;
import com.platypii.baseline.cloud.tasks.TaskTypes;
import com.platypii.baseline.tracks.TrackFile;
import android.content.Context;
import androidx.annotation.NonNull;
import com.google.gson.Gson;

public class TrackUploadTask implements Task {

    @NonNull
    private final TrackFile trackFile;

    public TrackUploadTask(@NonNull TrackFile trackFile) {
        this.trackFile = trackFile;
    }

    @Override
    public TaskType taskType() {
        return TaskTypes.trackUpload;
    }

    @Override
    public void run(@NonNull Context context) throws AuthRequiredException, UploadFailedException {
        UploadTask.upload(context, trackFile);
    }

    @Override
    public String toJson() {
        return new Gson().toJson(trackFile.file.getAbsolutePath());
    }

    @NonNull
    @Override
    public String toString() {
        return "TrackUpload(" + trackFile.getName() + ")";
    }

}
