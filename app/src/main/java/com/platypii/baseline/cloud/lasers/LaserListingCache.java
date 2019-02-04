package com.platypii.baseline.cloud.lasers;

import com.platypii.baseline.laser.LaserProfile;
import com.platypii.baseline.util.Exceptions;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.util.List;

/**
 * Local laser profile list cache, store in shared preferences.
 */
public class LaserListingCache {

    // Laser list local cache
    private static final String CACHE_LAST_REQUEST = "cloud.laser_list.request_time";
    private static final String CACHE_LAST_UPDATE = "cloud.laser_list.update_time";
    private static final String CACHE_LASER_LIST = "cloud.laser_list";

    // Minimum time between requests
    private static final long REQUEST_TTL = 30 * 1000; // milliseconds
    // Maximum lifetime of a successful listing
    private static final long UPDATE_TTL = 5 * 60 * 1000; // milliseconds

    private SharedPreferences prefs;

    void start(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Return laser listing from local cache, does NOT request from server, always returns fast.
     */
    @Nullable
    public List<LaserProfile> list() {
        if (prefs != null) {
            final String jsonString = prefs.getString(CACHE_LASER_LIST, null);
            if (jsonString != null) {
                try {
                    return new Gson().fromJson(jsonString, LaserListing.listType);
                } catch (JsonSyntaxException e) {
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
     * Set the laser listing cache
     */
    void update(@NonNull List<LaserProfile> lasers) {
        final String lasersJson = new Gson().toJson(lasers);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(CACHE_LAST_UPDATE, System.currentTimeMillis());
        editor.putString(CACHE_LASER_LIST, lasersJson);
        editor.apply();
    }

    /**
     * Return true if we should re-try listing
     */
    boolean shouldRequest() {
        // Compute time since last update
        final long lastUpdateDuration = System.currentTimeMillis() - prefs.getLong(CACHE_LAST_UPDATE, 0);
        final long lastRequestDuration = System.currentTimeMillis() - prefs.getLong(CACHE_LAST_REQUEST, 0);
        // Check that data is expired, and we haven't requested recently
        return UPDATE_TTL < lastUpdateDuration && REQUEST_TTL < lastRequestDuration;
    }

    public void clear() {
        final SharedPreferences.Editor editor = prefs.edit();
        editor.remove(CACHE_LAST_REQUEST);
        editor.remove(CACHE_LAST_UPDATE);
        editor.remove(CACHE_LASER_LIST);
        editor.apply();
    }

}
