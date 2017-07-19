package com.platypii.baseline.cloud;

import com.platypii.baseline.Services;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.util.IOUtil;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.firebase.crash.FirebaseCrash;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * List tracks from the cloud
 */
public class TrackListing {
    private static final String TAG = "TrackListing";

    public final TrackListingCache cache = new TrackListingCache();

    // Minimum time between requests
    private static final long REQUEST_TTL = 30 * 1000; // milliseconds
    // Maximum lifetime of a successful track listing
    private static final long UPDATE_TTL = 5 * 60 * 1000; // milliseconds

    /**
     * Query baseline server for track listing asynchronously
     */
    public void listAsync(final String auth, boolean force) {
        if(auth != null) {
            // Compute time since last update
            final long lastUpdateDuration = System.currentTimeMillis() - Services.prefs.getLong(TrackListingCache.CACHE_LAST_UPDATE, 0);
            final long lastRequestDuration = System.currentTimeMillis() - Services.prefs.getLong(TrackListingCache.CACHE_LAST_REQUEST, 0);
            final boolean shouldRequest = UPDATE_TTL < lastUpdateDuration && REQUEST_TTL < lastRequestDuration;
            if (force || shouldRequest) {
                final SharedPreferences.Editor editor = Services.prefs.edit();
                editor.putLong(TrackListingCache.CACHE_LAST_REQUEST, System.currentTimeMillis());
                editor.apply();
                // Update the track listing in a thread
                Log.i(TAG, "Listing tracks");
                new Thread() {
                    @Override
                    public void run() {
                        listTracks(auth);
                    }
                }.start();
            } else {
                final double t1 = lastUpdateDuration * 0.001;
                final double t2 = lastRequestDuration * 0.001;
                Log.d(TAG, String.format("Using cached track list (updated %.3fs, requested %.3fs)", t1, t2));
            }
        } else {
            Log.e(TAG, "Failed to list tracks, missing auth");
        }
    }

    /**
     * Notify listeners and handle exceptions
     */
    private void listTracks(String auth) {
        try {
            // Make HTTP request
            final List<CloudData> trackList = listRemote(auth);
            // Save track listing to local cache
            Services.cloud.listing.cache.update(trackList);
            // Notify listeners
            EventBus.getDefault().post(new SyncEvent.ListingSuccess());

            Log.i(TAG, "Listing successful: " + trackList.size() + " tracks");
        } catch(IOException e) {
            Log.e(TAG, "Failed to list tracks", e);
            FirebaseCrash.report(e);
        } catch(JSONException e) {
            Log.e(TAG, "Failed to parse response", e);
            FirebaseCrash.report(e);
        }
    }

    /**
     * Send http request to BASEline server for track listing
     */
    @NonNull
    private List<CloudData> listRemote(String auth) throws IOException, JSONException {
        final URL url = new URL(BaselineCloud.listUrl);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", auth);
        try {
            // Read response
            final int status = conn.getResponseCode();
            if(status == 200) {
                // Read body
                final String body = IOUtil.toString(conn.getInputStream());
                return fromJson(body);
            } else if(status == 401) {
                throw new AuthException(auth);
            } else {
                throw new IOException("http status code " + status);
            }
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Parse a json string into a list of track data
     */
    @NonNull
    static List<CloudData> fromJson(String json) throws JSONException {
        final ArrayList<CloudData> listing = new ArrayList<>();
        final JSONArray arr = new JSONArray(json);
        for(int i = 0; i < arr.length(); i++) {
            final JSONObject jsonObject = arr.getJSONObject(i);
            final CloudData cloudData = CloudData.fromJson(jsonObject);
            listing.add(cloudData);
        }
        return listing;
    }

    /**
     * Stringify a list of track data into a json string
     */
    static String toJson(@NonNull List<CloudData> trackList) {
        final JSONArray arr = new JSONArray();
        for(CloudData track : trackList) {
            final JSONObject trackObj = track.toJson();
            arr.put(trackObj);
        }
        return arr.toString();
    }

}
