package com.platypii.baseline.cloud.lasers;

import com.platypii.baseline.BaseService;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.cloud.RetrofitClient;
import com.platypii.baseline.cloud.cache.LaserCache;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.laser.LaserProfile;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
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

    private Context context;
    public final LaserCache cache = new LaserCache();

    @Override
    public void start(@NonNull Context context) {
        this.context = context;
        cache.start(context);
        listAsync(context, false);
        EventBus.getDefault().register(this);
    }

    /**
     * Query baseline server for laser listing asynchronously
     */
    void listAsync(@NonNull Context context, boolean force) {
        if (force || cache.shouldRequest()) {
            cache.request();
            final LaserApi laserApi = RetrofitClient.getRetrofit(context).create(LaserApi.class);
            // Public vs private based on sign in state
            final String userId = AuthState.getUser();
            Log.i(TAG, "Listing laser profiles for user " + userId);
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
     * Update laser listings on sign in
     */
    @Subscribe
    public void onSignIn(AuthState.SignedIn event) {
        listAsync(context, true);
    }

    /**
     * Clear the laser list cache and fetch public on sign out
     */
    @Subscribe
    public void onSignOut(AuthState.SignedOut event) {
        cache.clear();
        listAsync(context, true);
    }

    @Override
    public void stop() {
        EventBus.getDefault().unregister(this);
        context = null;
    }

}
