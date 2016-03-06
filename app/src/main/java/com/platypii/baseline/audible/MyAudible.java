package com.platypii.baseline.audible;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class MyAudible {
    private static final String TAG = "Audible";

    public static void initAudible(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs.getBoolean("audible_enabled", false)) {
            startAudible();
        }
    }

    public static void startAudible() {
        Log.i(TAG, "Starting audible");
    }

    public static void stopAudible() {
        Log.i(TAG, "Stopping audible");
    }

}
