package com.platypii.baseline.cloud;

import com.platypii.baseline.BaseService;
import com.platypii.baseline.BuildConfig;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.cache.TrackCache;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.util.IOUtil;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * List tracks from the cloud
 */
public class TrackListing implements BaseService {
    private static final String TAG = "TrackListing";

    private static final Type listType = new TypeToken<List<CloudData>>(){}.getType();

    public final TrackCache cache = new TrackCache();

    @Override
    public void start(@NonNull Context context) {
        cache.start(context);
        EventBus.getDefault().register(this);
    }

    /**
     * Query baseline server for track listing asynchronously
     */
    public void listAsync(@Nullable final String auth, boolean force) {
        if (auth != null) {
            if (force || cache.shouldRequest()) {
                cache.request();
                // Update the track listing in a thread
                Log.i(TAG, "Listing tracks");
                new Thread() {
                    @Override
                    public void run() {
                        listTracks(auth);
                    }
                }.start();
            }
        } else {
            Log.e(TAG, "Failed to list tracks, missing auth");
        }
    }

    /**
     * Notify listeners and handle exceptions
     */
    private void listTracks(String auth) {
        // Check for network availability. Still try to upload anyway, but don't report to firebase
        final boolean networkAvailable = Services.cloud.isNetworkAvailable();
        try {
            // Make HTTP request
            final List<CloudData> trackList = listRemote(auth);
            // Save track listing to local cache
            cache.update(trackList);
            // Notify listeners
            EventBus.getDefault().post(new SyncEvent.ListingSuccess());

            Log.i(TAG, "Listing successful: " + trackList.size() + " tracks");
        } catch (IOException e) {
            if (networkAvailable) {
                Log.e(TAG, "Failed to list tracks", e);
            } else {
                Log.w(TAG, "Failed to list tracks, network not available", e);
            }
        } catch (JsonSyntaxException e) {
            Exceptions.report(e);
        }
    }

    /**
     * Send http request to BASEline server for track listing
     */
    @NonNull
    private List<CloudData> listRemote(String auth) throws IOException, JsonSyntaxException {
        final URL url = new URL(BaselineCloud.listUrl);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", auth);
        conn.setRequestProperty("User-Agent", "BASEline Android App/" + BuildConfig.VERSION_NAME);
        try {
            // Read response
            final int status = conn.getResponseCode();
            if (status == 200) {
                // Read body
                final String body = IOUtil.toString(conn.getInputStream());
                return new Gson().fromJson(body, listType);
            } else if (status == 401) {
                throw new AuthException(auth);
            } else {
                throw new IOException("http status code " + status);
            }
        } finally {
            conn.disconnect();
        }
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
    }

}
