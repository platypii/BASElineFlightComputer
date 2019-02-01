package com.platypii.baseline.cloud;

import com.platypii.baseline.Services;
import com.platypii.baseline.events.LaserSyncEvent;
import com.platypii.baseline.laser.LaserProfile;
import com.platypii.baseline.util.Exceptions;
import android.content.Context;
import android.util.Log;
import java.io.IOException;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.greenrobot.eventbus.EventBus;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Upload laser profile to the cloud
 */
public class LaserUpload {
    private static final String TAG = "LaserUpload";

    private static LaserApi laserApi;

    private static LaserApi getLaserApi(Context context) {
        if (laserApi == null) {
            // Interceptor to add auth header
            final Interceptor authInterceptor = chain -> {
                Request request = chain.request();
                // Get auth token
                final String authToken = AuthToken.getAuthToken(context);
                final Headers headers = request.headers().newBuilder().add("Authorization", authToken).build();
                request = request.newBuilder().headers(headers).build();
                return chain.proceed(request);
            };
            final OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor)
                    .build();
            final Retrofit retrofit = new Retrofit.Builder()
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl(BaselineCloud.baselineServer)
                    .client(client)
                    .build();
            laserApi = retrofit.create(LaserApi.class);
        }
        return laserApi;
    }

    public static void post(Context context, LaserProfile laserProfile) {
        Log.i(TAG, "Uploading laser profile " + laserProfile);
        // Check for network availability. Still try to upload anyway, but don't report to firebase
        final boolean networkAvailable = Services.cloud.isNetworkAvailable();
        try {
            // Make HTTP request
            final LaserApi laserApi = getLaserApi(context);
            final Response<LaserProfile> response = laserApi.post(laserProfile).execute();
            Log.i(TAG, "Laser POST successful, laser profile " + response.body());
            EventBus.getDefault().post(new LaserSyncEvent.UploadSuccess());
        } catch (AuthException e) {
            if (networkAvailable) {
                Log.e(TAG, "Failed to upload file: auth error", e);
                Exceptions.report(e);
            } else {
                Log.w(TAG, "Failed to upload file: auth error", e);
            }
            EventBus.getDefault().post(new LaserSyncEvent.UploadFailure());
        } catch (IOException e) {
            if (networkAvailable) {
                Log.e(TAG, "Failed to upload file: io error", e);
                Exceptions.report(e);
            } else {
                Log.w(TAG, "Failed to upload file: io error", e);
            }
            EventBus.getDefault().post(new LaserSyncEvent.UploadFailure());
        }
    }

}
