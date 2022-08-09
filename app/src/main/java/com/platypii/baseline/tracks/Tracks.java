package com.platypii.baseline.tracks;

import com.platypii.baseline.BaseService;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.cloud.RetrofitClient;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.tracks.cloud.DeleteTask;
import com.platypii.baseline.tracks.cloud.TrackApi;
import com.platypii.baseline.util.Exceptions;

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

public class Tracks implements BaseService {
    private static final String TAG = "Tracks";

    public final TrackLogger logger = new TrackLogger();
    public final LocalTracks local = new LocalTracks();
    public final CloudTracks cloud = new CloudTracks();
    public final TrackCache cache = new TrackCache();
    final SyncManager sync = new SyncManager();

    // Starred track cache
    @Nullable
    private List<TrackData> starred;

    @Nullable
    private Context context;

    @Override
    public void start(@NonNull Context context) {
        this.context = context;
        logger.start(context);
        local.start(context);
        cache.start(context);
        sync.start(context);
        EventBus.getDefault().register(this);
        listAsync(context, false);
    }

    /**
     * Query baseline server for track listing asynchronously
     */
    public void listAsync(@Nullable Context context, boolean force) {
        if (context != null && AuthState.getUser() != null && (force || cache.shouldRequest())) {
            cache.request();
            try {
                final TrackApi trackApi = RetrofitClient.getRetrofit().create(TrackApi.class);
                Log.i(TAG, "Listing tracks");
                trackApi.list().enqueue(new Callback<List<TrackMetadata>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<TrackMetadata>> call, @NonNull Response<List<TrackMetadata>> response) {
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
                    public void onFailure(@NonNull Call<List<TrackMetadata>> call, @NonNull Throwable e) {
                        final boolean networkAvailable = Services.cloud.isNetworkAvailable();
                        if (networkAvailable) {
                            Log.e(TAG, "Failed to list tracks", e);
                        } else {
                            Log.w(TAG, "Failed to list tracks, network not available", e);
                        }
                    }
                });
            } catch (Throwable e) {
                Exceptions.report(e);
            }
        } else if (force) {
            Log.e(TAG, "Force listing called, but context or user unavailable " + context + " " + AuthState.getUser());
        }
    }

    public List<TrackData> getStarredTracks(Context context) {
        // Check cache
        if (starred != null) {
            return starred;
        }
        starred = new ArrayList<>();
        final List<TrackMetadata> tracks = Services.tracks.cache.list();
        if (tracks != null) {
            for (TrackMetadata track : tracks) {
                if (track.starred) {
                    final TrackData trackData = track.trackData(context);
                    if (trackData != null && trackData.stats.exit != null && trackData.stats.deploy != null) {
                        final TrackData trimmed = trackData.trim(trackData.stats.exit.millis, trackData.stats.deploy.millis);
                        starred.add(trimmed);
                    }
                }
            }
        }
        return starred;
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
        starred = null;
    }

    @Override
    public void stop() {
        EventBus.getDefault().unregister(this);
        sync.stop();
        cache.stop();
        local.stop();
        logger.stop();
        context = null;
    }

}
