package com.platypii.baseline.cloud;

import com.platypii.baseline.Services;
import com.platypii.baseline.events.SyncEvent;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.firebase.crash.FirebaseCrash;
import org.greenrobot.eventbus.EventBus;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Delete tracks from the cloud
 */
class DeleteTask implements Runnable {
    private static final String TAG = "DeleteTask";

    private final String auth;
    private final CloudData track;

    DeleteTask(@NonNull String auth, CloudData track) {
        this.auth = auth;
        this.track = track;
    }

    /**
     * Notify listeners and handle exceptions
     */
    @Override
    public void run() {
        Log.i(TAG, "Deleting track " + track.track_id);
        try {
            // Make HTTP request
            deleteRemote(auth, track.trackUrl);
            Log.i(TAG, "Track delete successful: " + track.track_id);
            // Update track list
            Services.cloud.listing.listAsync(auth, true);
            // Notify listeners
            EventBus.getDefault().post(new SyncEvent.DeleteSuccess(track.track_id));
        } catch(IOException e) {
            Log.e(TAG, "Failed to delete track " + track.track_id, e);
            // Notify listeners
            EventBus.getDefault().post(new SyncEvent.DeleteFailure(track.track_id, "failed to delete track"));
            // Report error
            FirebaseCrash.report(e);
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
            if(status != 200) {
                if (status == 401) {
                    throw new AuthException(auth);
                } else {
                    throw new IOException("http status code " + status);
                }
            }
        } finally {
            conn.disconnect();
        }
    }

}
