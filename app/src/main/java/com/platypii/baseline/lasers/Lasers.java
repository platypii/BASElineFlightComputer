package com.platypii.baseline.lasers;

import com.platypii.baseline.BaseService;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.cloud.RetrofitClient;
import com.platypii.baseline.cloud.cache.LaserCache;
import com.platypii.baseline.cloud.tasks.TaskType;
import com.platypii.baseline.events.LaserSyncEvent;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.lasers.cloud.LaserApi;
import com.platypii.baseline.lasers.cloud.LaserUploadTask;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.charts.layers.LaserProfileLayer;
import com.platypii.baseline.views.charts.layers.ProfileLayer;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * List lasers from the cloud
 */
public class Lasers implements BaseService {
    private static final String TAG = "Lasers";

    @Nullable
    private Context context;
    public final LaserCache cache = new LaserCache("cache");
    public final LaserCache unsynced = new LaserCache("unsynced");
    public final LaserLayers layers = new LaserLayers();

    @Override
    public void start(@NonNull Context context) {
        this.context = context;
        cache.start(context);
        listAsync(context, false);
        unsynced.start(context);
        uploadAll();
        EventBus.getDefault().register(this);
    }

    /**
     * Query baseline server for laser listing asynchronously
     */
    public void listAsync(@Nullable Context context, boolean force) {
        if (context != null && (force || cache.shouldRequest())) {
            cache.request();
            try {
                final LaserApi laserApi = RetrofitClient.getRetrofit(context).create(LaserApi.class);
                // Public vs private based on sign in state
                final String userId = AuthState.getUser();
                Log.i(TAG, "Listing laser profiles for user " + userId);
                final Call<List<LaserProfile>> laserCall = userId != null ? laserApi.byUser(userId) : laserApi.getPublic();
                laserCall.enqueue(new Callback<List<LaserProfile>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<LaserProfile>> call, @NonNull Response<List<LaserProfile>> response) {
                        final List<LaserProfile> lasers = response.body();
                        if (lasers != null) {
                            // Save laser listing to local cache
                            cache.update(lasers);
                            // Notify listeners
                            EventBus.getDefault().post(new LaserSyncEvent.ListingSuccess());
                            Log.i(TAG, "Listing successful: " + lasers.size() + " laser profiles");
                        } else {
                            Log.e(TAG, "Failed to list laser profiles, null");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<LaserProfile>> call, @NonNull Throwable e) {
                        final boolean networkAvailable = Services.cloud.isNetworkAvailable();
                        if (networkAvailable) {
                            Log.e(TAG, "Failed to list laser profiles", e);
                        } else {
                            Log.w(TAG, "Failed to list laser profiles, network not available", e);
                        }
                    }
                });
            } catch (Throwable e) {
                Exceptions.report(e);
            }
        }
    }

    public void addUnsynced(@NonNull LaserProfile laserProfile) {
        unsynced.add(laserProfile);
        if (AuthState.getUser() != null) {
            Services.tasks.add(new LaserUploadTask(laserProfile));
        }
    }

    /**
     * Get profile from cache (fallback to unsynced)
     */
    @Nullable
    public LaserProfile get(@NonNull String laser_id) {
        final LaserProfile fromCache = cache.get(laser_id);
        if (fromCache != null) {
            return fromCache;
        } else {
            return unsynced.get(laser_id);
        }
    }

    private void uploadAll() {
        if (AuthState.getUser() != null) {
            final List<LaserProfile> list = unsynced.list();
            if (list != null) {
                for (LaserProfile laserProfile : list) {
                    Services.tasks.add(new LaserUploadTask(laserProfile));
                }
            }
        }
    }

    /**
     * Update laser listings on sign in
     */
    @Subscribe
    public void onSignIn(@NonNull AuthState.SignedIn event) {
        listAsync(context, true);
        uploadAll();
    }

    /**
     * Clear the laser list cache and fetch public on sign out
     */
    @Subscribe
    public void onSignOut(@NonNull AuthState.SignedOut event) {
        cache.clear();
        Services.tasks.removeType(TaskType.laserUpload);
        listAsync(context, true);
    }

    @Subscribe
    public void onLaserListing(@NonNull LaserSyncEvent.ListingSuccess event) {
        // If lasers were deleted on server, layers should be removed.
        // Make a list of items to be removed, so that we don't modify list while iterating.
        final List<ProfileLayer> toRemove = new ArrayList<>();
        for (ProfileLayer layer : layers.layers) {
            if (layer instanceof LaserProfileLayer && cache.get(layer.id()) == null) {
                toRemove.add(layer);
            }
        }
        for (ProfileLayer layer : toRemove) {
            layers.remove(layer.id());
        }
    }

    @Subscribe
    public void onLaserDelete(@NonNull LaserSyncEvent.DeleteSuccess event) {
        // Remove from laser listing cache
        unsynced.remove(event.laserProfile);
        cache.remove(event.laserProfile);
        // Update laser list
        listAsync(context, true);
        // Remove from layers
        layers.remove(event.laserProfile.laser_id);
    }

    /**
     * Listen for track delete because we need to delete from laser layers.
     */
    @Subscribe
    public void onTrackDelete(@NonNull SyncEvent.DeleteSuccess event) {
        // Remove from layers
        layers.remove(event.track_id);
    }

    @Override
    public void stop() {
        EventBus.getDefault().unregister(this);
        Services.tasks.removeType(TaskType.laserUpload);
        context = null;
    }

}
