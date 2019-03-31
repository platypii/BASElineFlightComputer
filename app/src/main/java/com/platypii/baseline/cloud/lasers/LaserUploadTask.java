package com.platypii.baseline.cloud.lasers;

import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.tasks.AuthRequiredException;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.cloud.RetrofitClient;
import com.platypii.baseline.cloud.tasks.Task;
import com.platypii.baseline.cloud.tasks.TaskType;
import com.platypii.baseline.cloud.tasks.TaskTypes;
import com.platypii.baseline.laser.LaserProfile;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.gson.Gson;
import java.io.IOException;
import retrofit2.Response;

public class LaserUploadTask implements Task {
    private static final String TAG = "LaserUpload";

    @NonNull
    private final LaserProfile laserProfile;

    public LaserUploadTask(@NonNull LaserProfile laserProfile) {
        this.laserProfile = laserProfile;
    }

    @Override
    public TaskType taskType() {
        return TaskTypes.laserUpload;
    }

    @Override
    public void run(@NonNull Context context) throws AuthRequiredException, IOException {
        if (AuthState.getUser() == null) {
            throw new AuthRequiredException();
        }
        Log.i(TAG, "Uploading laser profile " + laserProfile);
        // Make HTTP request
        final LaserApi laserApi = RetrofitClient.getRetrofit(context).create(LaserApi.class);
        final Response<LaserProfile> response = laserApi.post(laserProfile).execute();
        if (response.isSuccessful()) {
            Log.i(TAG, "Laser POST successful, laser profile " + response.body());
            // Update laser listing
            Services.cloud.lasers.listAsync(context, true);
        } else {
            throw new IOException("Laser upload failed: " + response.errorBody().string());
        }
    }

    @Override
    public String toJson() {
        return new Gson().toJson(laserProfile);
    }

    @NonNull
    @Override
    public String toString() {
        return "LaserUpload(" + laserProfile.name + ")";
    }

}
