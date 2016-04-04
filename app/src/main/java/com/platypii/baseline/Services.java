package com.platypii.baseline;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.platypii.baseline.audible.MyAudible;
import com.platypii.baseline.data.Convert;
import com.platypii.baseline.data.KVStore;
import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyDatabase;
import com.platypii.baseline.data.MyLocationManager;
import com.platypii.baseline.data.MySensorManager;

/**
 * Start and stop essential services
 */
public class Services {
    private static final String TAG = "Services";

    // Count the number of times an activity has started.
    // This allows us to only stop services once the app is really done.
    private static int startCount = 0;

    public static void start(@NonNull BaseActivity activity) {
        startCount++;
        if(startCount == 1) {
            Log.i(TAG, "Starting services");
            final Context appContext = activity.getApplicationContext();

            // Start the various services

            Log.i(TAG, "Loading app settings");
            final SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(appContext);
            final boolean metricEnabled = prefs.getBoolean("metric_enabled", false);
            Convert.metric = metricEnabled;

            Log.i(TAG, "Starting key value store");
            KVStore.start(appContext);

            Log.i(TAG, "Starting location service");
            if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Enable location services
                try {
                    MyLocationManager.start(appContext);
                } catch (SecurityException e) {
                    Log.e(TAG, "Failed to start location service", e);
                }
            } else {
                // Request the missing permissions
                final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
                ActivityCompat.requestPermissions(activity, permissions, BaseActivity.MY_PERMISSIONS_REQUEST_LOCATION);
            }

            Log.i(TAG, "Initializing sensors");
            MySensorManager.start(appContext);

            Log.i(TAG, "Initializing altimeter");
            MyAltimeter.start(appContext);

            Log.i(TAG, "Checking for text-to-speech data");
            final Intent checkIntent = new Intent();
            checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
            activity.startActivityForResult(checkIntent, BaseActivity.MY_TTS_DATA_CHECK_CODE);
        } else {
            Log.i(TAG, "Services already started");
        }

        // TODO: Upload any unsynced files
        // TheCloud.uploadAll();
    }

    public static void stop() {
        startCount--;
        if(startCount == 0) {
            if(!MyDatabase.isLogging()) {
                Log.i(TAG, "All activities have stopped. Stopping services.");
                // Stop services
                MyAudible.terminate();
                MyAltimeter.stop();
                MyLocationManager.stop();
                KVStore.stop();
            } else {
                Log.w(TAG, "All activities have stopped, but still recording track. Leaving services running.");
            }
        }
    }

}
