package com.platypii.baseline.cloud.lasers;

import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.RetrofitClient;
import com.platypii.baseline.laser.LaserProfile;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import java.io.IOException;
import retrofit2.Response;

/**
 * Upload laser profile to the cloud
 */
public class LaserUpload {
    private static final String TAG = "LaserUpload";

    public static void post(@NonNull Context context, @NonNull LaserProfile laser) throws IOException {
        Log.i(TAG, "Uploading laser profile " + laser);
        // Make HTTP request
        final LaserApi laserApi = RetrofitClient.getRetrofit(context).create(LaserApi.class);
        final Response<LaserProfile> response = laserApi.post(laser).execute();
        if (response.isSuccessful()) {
            Log.i(TAG, "Laser POST successful, laser profile " + response.body());
            // Add to cloud cache // TODO: Use response.body()?
            Services.cloud.lasers.cache.add(laser);
            // Update laser listing
            Services.cloud.lasers.listAsync(context, true);
        }
    }

}
