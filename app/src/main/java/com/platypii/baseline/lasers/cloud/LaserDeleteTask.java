package com.platypii.baseline.lasers.cloud;

import com.platypii.baseline.cloud.RetrofitClient;
import com.platypii.baseline.events.LaserSyncEvent;
import com.platypii.baseline.lasers.LaserProfile;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.IOException;
import org.greenrobot.eventbus.EventBus;
import retrofit2.Response;

/**
 * Delete lasers from the cloud
 */
public class LaserDeleteTask implements Runnable {
    private static final String TAG = "LaserDelete";

    @NonNull
    private final Context context;
    @NonNull
    private final LaserProfile laser;

    public LaserDeleteTask(@NonNull Context context, @NonNull LaserProfile laser) {
        this.context = context;
        this.laser = laser;
    }

    @Override
    public void run() {
        Log.i(TAG, "Deleting laser " + laser);
        try {
            // Delete laser
            final LaserApi laserApi = RetrofitClient.getRetrofit().create(LaserApi.class);
            final Response<Void> response = laserApi.delete(laser.laser_id).execute();
            if (response.isSuccessful()) {
                Log.i(TAG, "Laser delete successful: " + laser);
                // Notify listeners
                EventBus.getDefault().post(new LaserSyncEvent.DeleteSuccess(laser));
            } else {
                Log.e(TAG, "Failed to delete laser " + laser + " " + response.code() + " " + response.errorBody());
                // Notify listeners
                EventBus.getDefault().post(new LaserSyncEvent.DeleteFailure(laser.laser_id, response.errorBody().string()));
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to delete laser " + laser + " " + e);
            // Notify listeners
            EventBus.getDefault().post(new LaserSyncEvent.DeleteFailure(laser.laser_id, e.getMessage()));
        }
    }

}
