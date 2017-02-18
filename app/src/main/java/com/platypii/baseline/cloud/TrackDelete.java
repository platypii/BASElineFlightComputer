package com.platypii.baseline.cloud;

import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.tracks.TrackData;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.firebase.crash.FirebaseCrash;
import org.greenrobot.eventbus.EventBus;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * List tracks from the cloud
 */
class TrackDelete {
    private static final String TAG = "TrackDelete";

    static void deleteAsync(@NonNull final String auth, final TrackData track) {
        Log.i(TAG, "Deleting track " + track.track_id);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                delete(auth, track);
            }
        });
    }

    /**
     * Notify listeners and handle exceptions
     */
    private static void delete(String auth, TrackData track) {
        try {
            // Make HTTP request
            deleteRemote(auth, track.track_url);
            Log.i(TAG, "Track delete successful: " + track.track_id);
            // Update track list
            TheCloud.invalidateCache();
            TheCloud.list(auth, true);
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
    private static void deleteRemote(String auth, String track_url) throws IOException {
        final URL url = new URL(track_url);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("Authorization", auth);
        try {
            // Read response
            final int status = conn.getResponseCode();
            if(status != 200) {
                if (status == 401) {
                    throw new IOException("authorization required");
                } else {
                    throw new IOException("http status code " + status);
                }
            }
        } finally {
            conn.disconnect();
        }
    }

}
