package com.platypii.baseline.cloud;

import com.platypii.baseline.events.SyncEvent;
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

    static void deleteAsync(@NonNull final String auth, final String track_url) {
        Log.i(TAG, "Deleting track " + track_url);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                delete(auth, track_url);
            }
        });
    }

    /**
     * Notify listeners and handle exceptions
     */
    private static void delete(String auth, String track_url) {
        try {
            // Make HTTP request
            deleteRemote(auth, track_url);
            Log.i(TAG, "Track delete successful: " + track_url);
            // Notify listeners
            EventBus.getDefault().post(new SyncEvent.ListingSuccess());
        } catch(IOException e) {
            Log.e(TAG, "Failed to delete track " + track_url, e);
            FirebaseCrash.report(e);
        }
    }

    /**
     * Send http delete to BASEline server
     */
    private static void deleteRemote(String auth, String track_url) throws IOException {
        final URL url = new URL(track_url);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
