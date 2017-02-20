package com.platypii.baseline.cloud;

import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.util.IOUtil;
import android.os.AsyncTask;
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
class TrackListing {
    private static final String TAG = "TrackListing";

    static void listTracksAsync(@NonNull final String auth) {
        Log.i(TAG, "Listing tracks");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                listTracks(auth);
            }
        });
    }

    /**
     * Notify listeners and handle exceptions
     */
    private static void listTracks(String auth) {
        try {
            // Make HTTP request
            final List<CloudData> trackList = listRemote(auth);
            // Save track listing to local cache
            BaselineCloud.updateCache(trackList);
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
    private static List<CloudData> listRemote(String auth) throws IOException, JSONException {
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
                throw new IOException("authorization required");
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
    static String toJson(List<CloudData> trackList) {
        final JSONArray arr = new JSONArray();
        for(CloudData track : trackList) {
            final JSONObject trackObj = track.toJson();
            arr.put(trackObj);
        }
        return arr.toString();
    }

}
