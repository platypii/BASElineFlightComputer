package com.platypii.baseline.cloud.tracks;

import com.platypii.baseline.BaseService;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.cloud.RetrofitClient;
import com.platypii.baseline.cloud.cache.TrackCache;
import com.platypii.baseline.events.SyncEvent;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Tracks implements BaseService {
    private static final String TAG = "Tracks";

    public final TrackCache cache = new TrackCache();
    private Context context;

    @Override
    public void start(@NonNull Context context) {
        this.context = context;
        cache.start(context);
        EventBus.getDefault().register(this);
    }

    /**
     * Query baseline server for track listing asynchronously
     */
    public void listAsync(@NonNull Context context, boolean force) {
        if (force || cache.shouldRequest()) {
            cache.request();
            final TrackApi trackApi = RetrofitClient.getRetrofit(context).create(TrackApi.class);
            Log.i(TAG, "Listing tracks");
            trackApi.list().enqueue(new Callback<List<CloudData>>() {
                @Override
                public void onResponse(Call<List<CloudData>> call, @NonNull Response<List<CloudData>> response) {
                    final List<CloudData> tracks = response.body();
                    if (tracks != null) {
                        // Save track listing to local cache
                        cache.update(tracks);
                        // Notify listeners
                        EventBus.getDefault().post(new SyncEvent.ListingSuccess());
                        Log.i(TAG, "Listing successful: " + tracks.size() + " tracks");
                    } else {
                        Log.e(TAG, "Failed to list tracks, null");
                    }
                }

                @Override
                public void onFailure(Call<List<CloudData>> call, Throwable e) {
                    final boolean networkAvailable = Services.cloud.isNetworkAvailable();
                    if (networkAvailable) {
                        Log.e(TAG, "Failed to list tracks", e);
                    } else {
                        Log.w(TAG, "Failed to list tracks, network not available", e);
                    }
                }
            });
        }
    }

    /**
     * Update track list on sign in
     */
    @Subscribe
    public void onSignIn(@NonNull AuthState.SignedIn event) {
        listAsync(context, false);
    }

    /**
     * Clear the track list cache when user signs out
     */
    @Subscribe
    public void onSignOut(@NonNull AuthState.SignedOut event) {
        cache.clear();
    }

    @Override
    public void stop() {
        EventBus.getDefault().unregister(this);
        context = null;
    }

}
