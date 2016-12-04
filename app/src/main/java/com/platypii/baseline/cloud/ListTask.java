package com.platypii.baseline.cloud;

import android.os.AsyncTask;
import android.util.Log;
import com.google.firebase.crash.FirebaseCrash;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.util.Callback;
import com.platypii.baseline.util.IOUtil;
import com.platypii.baseline.util.Try;
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
class ListTask extends AsyncTask<Void,Void,Try<List<CloudData>>> {
    private static final String TAG = "CloudUpload";

    private static final String listUrl = TheCloud.baselineServer + "/v0/tracks";

    private final String auth;
    private final Callback<List<CloudData>> cb;

    ListTask(String auth, Callback<List<CloudData>> cb) {
        this.auth = auth;
        this.cb = cb;
    }

    @Override
    protected Try<List<CloudData>> doInBackground(Void... voids) {
        Log.i(TAG, "Listing tracks with auth " + auth);
        try {
            // Make HTTP request
            final List<CloudData> result = listTracks(auth);
            // TODO: Save cloud data
            Log.i(TAG, "Listing successful: " + result.size());
            return new Try.Success<>(result);
        } catch(IOException e) {
            Log.e(TAG, "Failed to list tracks", e);
            FirebaseCrash.report(e);
            return new Try.Failure<>(e.getMessage());
        } catch(JSONException e) {
            Log.e(TAG, "Failed to parse response", e);
            FirebaseCrash.report(e);
            return new Try.Failure<>(e.toString());
        }
    }
    @Override
    protected void onPostExecute(Try<List<CloudData>> result) {
        EventBus.getDefault().post(new SyncEvent());
        if(cb != null) {
            if(result instanceof Try.Success) {
                final List<CloudData> cloudData = ((Try.Success<List<CloudData>>) result).result;
                cb.apply(cloudData);
            } else {
                final String error = ((Try.Failure<List<CloudData>>) result).error;
                cb.error(error);
            }
        }
    }

    private static List<CloudData> listTracks(String auth) throws IOException, JSONException {
        final URL url = new URL(listUrl);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Authorization", auth);
        try {
            // Read response
            final int status = conn.getResponseCode();
            if(status == 200) {
                // Read body
                final String body = IOUtil.toString(conn.getInputStream());
                return parseListing(body);
            } else if(status == 401) {
                throw new IOException("authorization required");
            } else {
                throw new IOException("http status code " + status);
            }
        } finally {
            conn.disconnect();
        }
    }

    private static List<CloudData> parseListing(String json) throws JSONException {
        final ArrayList<CloudData> results = new ArrayList<>();
        final JSONArray jsonArray = new JSONArray(json);
        for(int i = 0; i < jsonArray.length(); i++) {
            final JSONObject jsonObject = jsonArray.getJSONObject(i);
            final CloudData cloudData = CloudData.fromJson(jsonObject);
            results.add(cloudData);
        }
        return results;
    }

}
