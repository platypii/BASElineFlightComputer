package com.platypii.baseline.cloud.lasers;

import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.RetrofitClient;
import com.platypii.baseline.events.LaserDeleteEvent;
import com.platypii.baseline.laser.LaserLayers;
import com.platypii.baseline.laser.LaserProfile;

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
    private LaserProfile laser;

    public LaserDeleteTask(@NonNull Context context, @NonNull LaserProfile laser) {
        this.context = context;
        this.laser = laser;
    }

    @Override
    public void run() {
        Log.i(TAG, "Deleting laser " + laser.laser_id);
        // Check for network availability. Still try to delete anyway, but don't report to firebase
        try {
            // Delete laser
            final LaserApi laserApi = RetrofitClient.getRetrofit(context).create(LaserApi.class);
            final Response response = laserApi.delete(laser.laser_id).execute();
            if (response.isSuccessful()) {
                Log.i(TAG, "Laser delete successful: " + laser.laser_id);
                // Remove from laser layers
                LaserLayers.getInstance().removeById(laser.laser_id);
                // Remove from laser listing cache
                Services.cloud.lasers.cache.remove(laser);
                // Update laser list
                Services.cloud.lasers.listAsync(context, true);
                // Notify listeners
                EventBus.getDefault().post(new LaserDeleteEvent.LaserDeleteSuccess(laser.laser_id));
            } else {
                Log.e(TAG, "Failed to delete laser " + laser.laser_id + " " + response.errorBody());
                // Notify listeners
                EventBus.getDefault().post(new LaserDeleteEvent.LaserDeleteFailure(laser.laser_id, response.errorBody().string()));
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to delete laser " + laser.laser_id, e);
            // Notify listeners
            EventBus.getDefault().post(new LaserDeleteEvent.LaserDeleteFailure(laser.laser_id, e.getMessage()));
        }
    }

}
