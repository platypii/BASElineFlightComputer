package com.platypii.baseline.cloud;

import com.platypii.baseline.util.Exceptions;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;

/**
 * Local track list cache, store in shared preferences.
 */
public class TrackListingCache {

    // Track list local cache
    private static final String CACHE_LAST_REQUEST = "cloud.track_list.request_time";
    private static final String CACHE_LAST_UPDATE = "cloud.track_list.update_time";
    private static final String CACHE_TRACK_LIST = "cloud.track_list";

    // Minimum time between requests
    private static final long REQUEST_TTL = 30 * 1000; // milliseconds
    // Maximum lifetime of a successful track listing
    private static final long UPDATE_TTL = 5 * 60 * 1000; // milliseconds

    private SharedPreferences prefs;

    void start(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Return track listing from local cache, does NOT request from server, always returns fast.
     */
    @Nullable
    public List<CloudData> list() {
        if (prefs != null) {
            final String jsonString = prefs.getString(CACHE_TRACK_LIST, null);
            if (jsonString != null) {
                try {
                    return TrackListing.fromJson(jsonString);
                } catch (JSONException e) {
                    Exceptions.report(e);
                }
            }
        }
        return null;
    }

    /**
     * Update the last request time with the current time
     */
    void request() {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(CACHE_LAST_REQUEST, System.currentTimeMillis());
        editor.apply();
    }

    /**
     * Add track to track listing, and save to preferences
     */
    void addTrack(@NonNull CloudData trackData) {
        List<CloudData> trackList = list();
        if (trackList == null) {
            trackList = new ArrayList<>();
        }
        trackList.add(trackData);
        update(trackList);
    }

    /**
     * Return the most recent track data available
     */
    @Nullable
    public CloudData getTrack(@NonNull String track_id) {
        final List<CloudData> tracks = list();
        if (tracks != null) {
            for (CloudData track : tracks) {
                if (track.track_id.equals(track_id)) {
                    return track;
                }
            }
        }
        return null;
    }

    /**
     * Add track to track listing, and save to preferences
     */
    void removeTrack(@NonNull CloudData track) {
        final List<CloudData> tracks = list();
        if (tracks != null) {
            if (tracks.remove(track)) {
                update(tracks);
            }
        }
    }

    /**
     * Set the track listing cache
     */
    void update(@NonNull List<CloudData> trackList) {
        final String trackListJson = TrackListing.toJson(trackList);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(CACHE_LAST_UPDATE, System.currentTimeMillis());
        editor.putString(CACHE_TRACK_LIST, trackListJson);
        editor.apply();
    }

    /**
     * Return true if we should re-try track listing
     */
    boolean shouldRequest() {
        // Compute time since last update
        final long lastUpdateDuration = System.currentTimeMillis() - prefs.getLong(CACHE_LAST_UPDATE, 0);
        final long lastRequestDuration = System.currentTimeMillis() - prefs.getLong(CACHE_LAST_REQUEST, 0);
        // Check that data is expired, and we haven't requested recently
        return UPDATE_TTL < lastUpdateDuration && REQUEST_TTL < lastRequestDuration;
    }

    void clear() {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.remove(CACHE_LAST_REQUEST);
        editor.remove(CACHE_LAST_UPDATE);
        editor.remove(CACHE_TRACK_LIST);
        editor.apply();
    }

}
