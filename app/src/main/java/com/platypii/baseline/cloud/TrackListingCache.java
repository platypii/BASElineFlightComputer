package com.platypii.baseline.cloud;

import com.platypii.baseline.Services;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import com.google.firebase.crash.FirebaseCrash;
import java.util.List;
import org.json.JSONException;

/**
 * Local track list cache, store in shared preferences.
 */
public class TrackListingCache {

    // Track list local cache
    static final String CACHE_LAST_REQUEST = "cloud.track_list.request_time";
    static final String CACHE_LAST_UPDATE = "cloud.track_list.update_time";
    private static final String CACHE_TRACK_LIST = "cloud.track_list";

    /**
     * Return track listing from local cache, does NOT request from server, always returns fast.
     */
    public List<CloudData> list() {
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

    void update(@NonNull List<CloudData> trackList) {
        final String trackListJson = TrackListing.toJson(trackList);
        final SharedPreferences.Editor editor = Services.prefs.edit();
        editor.putLong(CACHE_LAST_UPDATE, System.currentTimeMillis());
        editor.putString(CACHE_TRACK_LIST, trackListJson);
        editor.apply();
    }

    void clear() {
        final SharedPreferences.Editor editor = Services.prefs.edit();
        editor.remove(CACHE_LAST_REQUEST);
        editor.remove(CACHE_LAST_UPDATE);
        editor.remove(CACHE_TRACK_LIST);
        editor.apply();
    }

}
