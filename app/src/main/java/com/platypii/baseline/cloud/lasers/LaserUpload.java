package com.platypii.baseline.cloud.lasers;

import com.platypii.baseline.cloud.RetrofitClient;
import com.platypii.baseline.laser.LaserProfile;
import android.content.Context;
import android.util.Log;
import java.io.IOException;
import retrofit2.Response;

/**
 * Upload laser profile to the cloud
 */
public class LaserUpload {
    private static final String TAG = "LaserUpload";

    public static void post(Context context, LaserProfile laserProfile) throws IOException {
        Log.i(TAG, "Uploading laser profile " + laserProfile);
        // Make HTTP request
        final LaserApi laserApi = RetrofitClient.getRetrofit(context).create(LaserApi.class);
        final Response<LaserProfile> response = laserApi.post(laserProfile).execute();
        Log.i(TAG, "Laser POST successful, laser profile " + response.body());
    }

}
