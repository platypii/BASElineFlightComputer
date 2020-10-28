package com.platypii.baseline.tracks.cloud;

import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.RetrofitClient;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.tracks.TrackMetadata;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.IOException;
import org.greenrobot.eventbus.EventBus;
import retrofit2.Response;

/**
 * Delete tracks from the cloud
 */
public class DeleteTask implements Runnable {
    private static final String TAG = "DeleteTask";

    @NonNull
    private final Context context;
    @NonNull
    private final TrackMetadata track;

    public DeleteTask(@NonNull Context context, @NonNull TrackMetadata track) {
        this.context = context.getApplicationContext();
        this.track = track;
    }

    /**
     * Send http delete to BASEline server
     */
    @Override
    public void run() {
        Log.i(TAG, "Deleting track " + track.track_id);
        try {
            // Delete track
            final TrackApi trackApi = RetrofitClient.getRetrofit(context).create(TrackApi.class);
            final Response<Void> response = trackApi.delete(track.track_id).execute();
            if (response.isSuccessful()) {
                Log.i(TAG, "Track delete successful: " + track.track_id);
                // Remove from track listing cache
                Services.tracks.cache.remove(track);
                // Update track list
                Services.tracks.listAsync(context, true);
                // Notify listeners
                EventBus.getDefault().post(new SyncEvent.DeleteSuccess(track.track_id));
            } else {
                Log.e(TAG, "Failed to delete track " + track.track_id + " " + response.code() + " " + response.errorBody());
                // Notify listeners
                EventBus.getDefault().post(new SyncEvent.DeleteFailure(track.track_id, response.errorBody().string()));
            }
        } catch (IOException e) {
            Log.e(TAG, "Failed to delete track " + track + " " + e);
            // Notify listeners
            EventBus.getDefault().post(new SyncEvent.DeleteFailure(track.track_id, e.getMessage()));
        }
    }

}
