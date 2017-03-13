package com.platypii.baseline.cloud;

import com.platypii.baseline.Services;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.util.Callback;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.firebase.crash.FirebaseCrash;
import org.json.JSONException;
import java.util.List;

public class BaselineCloud {
    private static final String TAG = "BaselineCloud";

    static final String baselineServer = "https://base-line.ws";
    static final String listUrl = BaselineCloud.baselineServer + "/v1/tracks";

    // Track list local cache
    private static final String CACHE_LAST_REQUEST = "cloud.track_list.request_time";
    private static final String CACHE_LAST_UPDATE = "cloud.track_list.update_time";
    private static final String CACHE_TRACK_LIST = "cloud.track_list";
    // Minimum time between requests
    private static final long REQUEST_TTL = 30 * 1000; // milliseconds
    // Maximum lifetime of a successful track listing
    private static final long UPDATE_TTL = 5 * 60 * 1000; // milliseconds

    public static CloudData getCached(String track_id) {
        final List<CloudData> tracks = listCached();
        if(tracks != null) {
            for(CloudData track : tracks) {
                if(track.track_id.equals(track_id)) {
                    return track;
                }
            }
        }
        return null;
    }

    public static List<CloudData> listCached() {
        if(Services.prefs != null) {
            final String jsonString = Services.prefs.getString(CACHE_TRACK_LIST, null);
            if (jsonString != null) {
                try {
                    return TrackListing.fromJson(jsonString);
                } catch (JSONException e) {
                    FirebaseCrash.report(e);
                }
            }
        }
        return null;
    }

    static void updateCache(List<CloudData> trackList) {
        final String trackListJson = TrackListing.toJson(trackList);
        final SharedPreferences.Editor editor = Services.prefs.edit();
        editor.putLong(CACHE_LAST_UPDATE, System.currentTimeMillis());
        editor.putString(CACHE_TRACK_LIST, trackListJson);
        editor.apply();
    }

    static void addTrackData(CloudData trackData) {
        final List<CloudData> trackList = listCached();
        if(trackList != null) {
            trackList.add(trackData);
            updateCache(trackList);
        } else {
            // TODO: Save singleton track list
            Log.e(TAG, "Failed to add to null track list");
        }
    }

    /**
     * Clear the track list cache (for when user signs out)
     */
    public static void signOut() {
        final SharedPreferences.Editor editor = Services.prefs.edit();
        editor.remove(CACHE_LAST_REQUEST);
        editor.remove(CACHE_LAST_UPDATE);
        editor.remove(CACHE_TRACK_LIST);
        editor.apply();
    }

    /**
     * Query baseline server for track listing asynchronously
     */
    public static void listAsync(String auth, boolean force) {
        if(auth != null) {
            // Compute time since last update
            final long lastUpdateDuration = System.currentTimeMillis() - Services.prefs.getLong(CACHE_LAST_UPDATE, 0);
            final long lastRequestDuration = System.currentTimeMillis() - Services.prefs.getLong(CACHE_LAST_REQUEST, 0);
            final boolean shouldRequest = UPDATE_TTL < lastUpdateDuration && REQUEST_TTL < lastRequestDuration;
            if (force || shouldRequest) {
                final SharedPreferences.Editor editor = Services.prefs.edit();
                editor.putLong(CACHE_LAST_REQUEST, System.currentTimeMillis());
                editor.apply();
                // Update the track listing
                TrackListing.listTracksAsync(auth);
            } else {
                final double t1 = lastUpdateDuration * 0.001;
                final double t2 = lastRequestDuration * 0.001;
                Log.d(TAG, String.format("Using cached track list (updated %.3fs, requested %.3fs)", t1, t2));
            }
        } else {
            Log.e(TAG, "Failed to list tracks, missing auth");
        }
    }

    public static void upload(TrackFile trackFile, String auth, Callback<CloudData> cb) {
        new UploadTask(trackFile, auth, cb).execute();
    }

    public static void deleteTrack(CloudData track, String auth) {
        // Delete track on server
        TrackDelete.deleteAsync(auth, track);
    }
}
