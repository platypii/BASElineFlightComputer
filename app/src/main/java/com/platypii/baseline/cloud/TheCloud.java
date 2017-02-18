package com.platypii.baseline.cloud;

import com.platypii.baseline.Services;
import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.util.Callback;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.firebase.crash.FirebaseCrash;
import org.json.JSONException;
import java.util.List;

public class TheCloud {
    private static final String TAG = "TheCloud";

    static final String baselineServer = "https://base-line.ws";
    static final String listUrl = TheCloud.baselineServer + "/v1/tracks";

    private static final String CACHE_DATE = "cloud.track_list.date";
    private static final String CACHE_TRACK_LIST = "cloud.track_list";
    private static final long TRACK_LIST_TTL = 5 * 60 * 1000; // milliseconds

    public static TrackData getCached(String track_id) {
        final List<TrackData> tracks = listCached();
        if(tracks != null) {
            for(TrackData track : tracks) {
                if(track.track_id.equals(track_id)) {
                    return track;
                }
            }
        }
        return null;
    }

    public static List<TrackData> listCached() {
        final String jsonString = Services.prefs.getString(CACHE_TRACK_LIST, null);
        if(jsonString != null) {
            try {
                return TrackListing.fromJson(jsonString);
            } catch (JSONException e) {
                FirebaseCrash.report(e);
                return null;
            }
        } else {
            return null;
        }
    }

    static void updateCache(String trackListJson) {
        final SharedPreferences.Editor editor = Services.prefs.edit();
        editor.putString(TheCloud.CACHE_TRACK_LIST, trackListJson);
        editor.apply();
    }

    /**
     * Clear the track list cache (for when user signs out)
     */
    static void invalidateCache() {
        final SharedPreferences.Editor editor = Services.prefs.edit();
        editor.remove(CACHE_DATE);
        // editor.remove(CACHE_TRACK_LIST);
        editor.apply();
    }

    /**
     * Query baseline server for track listing asynchronously
     */
    public static void list(@NonNull String auth, boolean force) {
        // Compute time since last update
        final long lastUpdate = System.currentTimeMillis() - Services.prefs.getLong(CACHE_DATE, 0);
        if(force || TRACK_LIST_TTL < lastUpdate) {
            final SharedPreferences.Editor editor = Services.prefs.edit();
            editor.putLong(CACHE_DATE, System.currentTimeMillis());
            editor.apply();
            // Update the track listing
            TrackListing.listTracksAsync(auth);
        } else {
            final double t = lastUpdate * 0.001;
            Log.d(TAG, String.format("Using cached track list (%.3fs)", t));
        }
    }

    public static void upload(TrackFile trackFile, String auth, Callback<CloudData> cb) {
        new UploadTask(trackFile, auth, cb).execute();
    }

    public static void deleteTrack(TrackData track, String auth) {
        // Delete track on server
        TrackDelete.deleteAsync(auth, track);
    }
}
