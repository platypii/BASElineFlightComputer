package com.platypii.baseline.tracks;

import com.platypii.baseline.BaseService;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.cloud.RetrofitClient;
import com.platypii.baseline.cloud.cache.TrackCache;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.tracks.cloud.DeleteTask;
import com.platypii.baseline.tracks.cloud.TrackApi;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Tracks implements BaseService {
    private static final String TAG = "Tracks";

    public final TrackLogger logger = new TrackLogger();
    public final TrackStore store = new TrackStore();
    public final TrackCache cache = new TrackCache();
    final UploadManager uploads = new UploadManager();

    @Nullable
    private Context context;

    @Override
    public void start(@NonNull Context context) {
        this.context = context;
        logger.start(context);
        store.start(context);
        cache.start(context);
        uploads.start(context);
        EventBus.getDefault().register(this);
        listAsync(context, false);
    }

    /**
     * Query baseline server for track listing asynchronously
     */
    public void listAsync(@Nullable Context context, boolean force) {
        if (context != null && AuthState.getUser() != null && (force || cache.shouldRequest())) {
            cache.request();
            final TrackApi trackApi = RetrofitClient.getRetrofit(context).create(TrackApi.class);
            Log.i(TAG, "Listing tracks");
            trackApi.list().enqueue(new Callback<List<TrackMetadata>>() {
                @Override
                public void onResponse(Call<List<TrackMetadata>> call, @NonNull Response<List<TrackMetadata>> response) {
                    final List<TrackMetadata> tracks = response.body();
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
                public void onFailure(Call<List<TrackMetadata>> call, Throwable e) {
                    final boolean networkAvailable = Services.cloud.isNetworkAvailable();
                    if (networkAvailable) {
                        Log.e(TAG, "Failed to list tracks", e);
                    } else {
                        Log.w(TAG, "Failed to list tracks, network not available", e);
                    }
                }
            });
        } else if (force) {
            Log.e(TAG, "Force listing called, but context or user unavailable " + context + " " + AuthState.getUser());
        }
    }

    public void deleteTrack(@NonNull Context context, @NonNull TrackMetadata track) {
        new Thread(new DeleteTask(context, track)).start();
    }

    /**
     * Update track list on sign in
     */
    @Subscribe
    public void onSignIn(@NonNull AuthState.SignedIn event) {
        listAsync(context, true);
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
        uploads.stop();
        cache.stop();
        store.stop();
        logger.stop();
        context = null;
    }

}
