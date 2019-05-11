package com.platypii.baseline.cloud;

import com.platypii.baseline.Services;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.util.Exceptions;
import android.content.Context;
import androidx.annotation.NonNull;
import android.util.Log;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.greenrobot.eventbus.EventBus;

/**
 * Delete tracks from the cloud
 */
class DeleteTask implements Runnable {
    private static final String TAG = "DeleteTask";

    @NonNull
    private final Context context;
    @NonNull
    private final CloudData track;

    DeleteTask(@NonNull Context context, @NonNull CloudData track) {
        this.context = context;
        this.track = track;
    }

    /**
     * Notify listeners and handle exceptions
     */
    @Override
    public void run() {
        Log.i(TAG, "Deleting track " + track.track_id);
        // Check for network availability. Still try to delete anyway, but don't report to firebase
        final boolean networkAvailable = Services.cloud.isNetworkAvailable();
        try {
            // Get auth token
            final String auth = AuthToken.getAuthToken(context);
            // Make HTTP request
            deleteRemote(auth, track.trackUrl);
            Log.i(TAG, "Track delete successful: " + track.track_id);
            // Remove from track listing cache
            Services.cloud.listing.cache.remove(track);
            // Update track list
            Services.cloud.listing.listAsync(auth, true);
            // Notify listeners
            EventBus.getDefault().post(new SyncEvent.DeleteSuccess(track.track_id));
        } catch (IOException e) {
            Log.e(TAG, "Failed to delete track " + track.track_id, e);
            // Notify listeners
            EventBus.getDefault().post(new SyncEvent.DeleteFailure(track.track_id, "failed to delete track"));
            if (networkAvailable) {
                Exceptions.report(e);
            }
        }
    }

    /**
     * Send http delete to BASEline server
     */
    private static void deleteRemote(String auth, String trackUrl) throws IOException {
        final URL url = new URL(trackUrl);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Authorization", auth);
        try {
            // Read response
            final int status = conn.getResponseCode();
            if (status != 200) {
                if (status == 401) {
                    throw new AuthException(auth);
                } else {
                    throw new IOException("http status " + status + " " + trackUrl);
                }
            }
        } finally {
            conn.disconnect();
        }
    }

}
