package com.platypii.baseline.cloud;

import com.platypii.baseline.util.Callback;
import com.platypii.baseline.util.IOUtil;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Fetch geo data for a given track
 */
class TrackDataTask extends AsyncTask<Void,Void,List<Location>> {
    private static final String TAG = "TrackDataTask";

    private final String track_id;
    private final Callback<List<Location>> cb;

    TrackDataTask(String track_id, Callback<List<Location>> cb) {
        this.track_id = track_id;
        this.cb = cb;
    }

    @Override
    protected List<Location> doInBackground(Void... voids) {
        Log.i(TAG, "Fetching geo data for track " + track_id);
        try {
            // Make HTTP request
            final List<Location> result = fetchGeoData(track_id);
            Log.i(TAG, "Fetch geo data successful: " + result.size());
            return result;
        } catch(IOException e) {
            Log.e(TAG, "Failed to fetch geo data", e);
            return null;
        } catch(JSONException e) {
            Log.e(TAG, "Failed to parse response", e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<Location> trackData) {
        if(cb != null) {
            if(trackData != null) {
                cb.apply(trackData);
            } else {
                cb.error("Failed to fetch track data");
            }
        }
    }

    private static String geoDataUrl(String track_id) {
        return BaselineCloud.baselineServer + "/v1/tracks/" + track_id + "/geodata";
    }

    /**
     * Make http request to BASEline server for track listing
     */
    private static List<Location> fetchGeoData(String track_id) throws IOException, JSONException {
        final URL url = new URL(geoDataUrl(track_id));
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        // conn.setRequestProperty("Authorization", auth);
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

    /**
     * Parse raw json into a list of geo data
     */
    private static List<Location> parseListing(String jsonString) throws JSONException {
        final JSONObject json = new JSONObject(jsonString);
        final JSONArray jsonArray = json.getJSONArray("data");

        final ArrayList<Location> points = new ArrayList<>();
        for(int i = 0; i < jsonArray.length(); i++) {
            final JSONObject jsonObject = jsonArray.getJSONObject(i);
            final Location point = parseLocation(jsonObject);
            points.add(point);
        }
        return points;
    }

    private static Location parseLocation(JSONObject json) throws JSONException {
        final Location loc = new Location("l");
        loc.setTime(json.getLong("millis"));
        loc.setLatitude(json.getDouble("lat"));
        loc.setLongitude(json.getDouble("lon"));
        loc.setAltitude(json.getDouble("alt"));
        return loc;
    }

}