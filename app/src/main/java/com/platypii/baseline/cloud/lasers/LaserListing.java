package com.platypii.baseline.cloud.lasers;

import com.platypii.baseline.BaseService;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.cloud.RetrofitClient;
import com.platypii.baseline.cloud.cache.LaserCache;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.laser.LaserProfile;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * List lasers from the cloud
 */
public class LaserListing implements BaseService {
    private static final String TAG = "LaserListing";

    static final Type listType = new TypeToken<List<LaserProfile>>(){}.getType();

    public final LaserCache cache = new LaserCache();

    @Override
    public void start(@NonNull Context context) {
        cache.start(context);
        EventBus.getDefault().register(this);
    }

    /**
     * Query baseline server for laser listing asynchronously
     */
    public void listAsync(@NonNull Context context, boolean force) {
        if (force || cache.shouldRequest()) {
            cache.request();
            Log.i(TAG, "Listing laser profiles");
            final LaserApi laserApi = RetrofitClient.getRetrofit(context).create(LaserApi.class);
            // Public vs private based on sign in state
            final String userId = AuthState.getUser();
            final Call<List<LaserProfile>> laserCall = userId != null ? laserApi.byUser(userId) : laserApi.getPublic();
            laserCall.enqueue(new Callback<List<LaserProfile>>() {
                @Override
                public void onResponse(Call<List<LaserProfile>> call, Response<List<LaserProfile>> response) {
                    final List<LaserProfile> lasers = response.body();
                    // Save laser listing to local cache
                    cache.update(lasers);
                    // Notify listeners
                    EventBus.getDefault().post(new SyncEvent.ListingSuccess());
                    Log.i(TAG, "Listing successful: " + lasers.size() + " laser profiles");
                }

                @Override
                public void onFailure(Call<List<LaserProfile>> call, Throwable e) {
                    final boolean networkAvailable = Services.cloud.isNetworkAvailable();
                    if (networkAvailable) {
                        Log.e(TAG, "Failed to list laser profiles", e);
                    } else {
                        Log.w(TAG, "Failed to list laser profiles, network not available", e);
                    }
                }
            });
        }
    }

    /**
     * Clear the laser cache when user signs out
     */
    @Subscribe
    public void onSignOut(@NonNull AuthState.SignedOut event) {
        cache.clear();
    }

    @Override
    public void stop() {
        EventBus.getDefault().unregister(this);
    }

}
