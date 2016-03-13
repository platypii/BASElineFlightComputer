package com.platypii.baseline.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

public class KVStore {

    private static SharedPreferences prefs;
    private static boolean started = false;

    public static synchronized void start(Context appContext) {
        // Load shared preferences for persistence
        if(started) {
            Log.e("KVStore", "Already started");
        }
        prefs = appContext.getSharedPreferences("baseline", Context.MODE_PRIVATE);
        started = true;
    }

    public static String getString(String key) {
        if(started) {
            return prefs.getString(key, null);
        } else {
            Log.e("KVStore", "Get attempted on uninitialized key/value store");
            return null;
        }
    }

    public static void put(String key, String value) {
        if(started) {
            final SharedPreferences.Editor editor = prefs.edit();
            editor.putString(key, value);
            editor.apply();
        } else {
            Log.e("KVStore", "Put attempted on uninitialized key/value store");
        }
    }

    /**
     * Stop the key value store service and free resources
     */
    public static void stop() {
        started = false;
        prefs = null;
    }

}
