package com.platypii.baseline;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Abstraction over android's preferences to implement a simple key value store, with string types
 */
public class KVStore implements Service {
    private static final String TAG = "KVStore";

    private SharedPreferences prefs;
    private boolean started = false;

    @Override
    public synchronized void start(@NonNull Context context) {
        // Load shared preferences for persistence
        if(started) {
            Log.e(TAG, "Already started");
        }
        prefs = context.getSharedPreferences("baseline", Context.MODE_PRIVATE);
        started = true;
    }

    public String getString(String key) {
        if(started) {
            return prefs.getString(key, null);
        } else {
            Log.e(TAG, "Get attempted on uninitialized key/value store");
            return null;
        }
    }

    public void put(String key, String value) {
        if(started) {
            final SharedPreferences.Editor editor = prefs.edit();
            editor.putString(key, value);
            editor.apply();
        } else {
            Log.e(TAG, "Put attempted on uninitialized key/value store");
        }
    }

    /**
     * Stop the key value store service and free resources
     */
    @Override
    public void stop() {
        started = false;
        prefs = null;
    }

}
