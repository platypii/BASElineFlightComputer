package com.platypii.baseline;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.platypii.baseline.audible.MyAudible;
import com.platypii.baseline.bluetooth.BluetoothService;
import com.platypii.baseline.data.Convert;
import com.platypii.baseline.data.KVStore;
import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyDatabase;
import com.platypii.baseline.data.MyFlightManager;
import com.platypii.baseline.location.LocationService;
import com.platypii.baseline.data.MySensorManager;
import com.platypii.baseline.util.Util;

/**
 * Start and stop essential services
 */
public class Services {
    private static final String TAG = "Services";

    // Count the number of times an activity has started.
    // This allows us to only stop services once the app is really done.
    private static int startCount = 0;
    private static boolean initialized = false;

    // How long to wait after the last activity shutdown to restart services
    final static Handler handler = new Handler();
    private static final int shutdownDelay = 10000;

    public static LocationService location;

    public static void start(@NonNull Activity activity) {
        startCount++;
        if(!initialized) {
            initialized = true;
            Log.i(TAG, "Starting services");
            final Context appContext = activity.getApplicationContext();

            handler.removeCallbacks(stopRunnable);

            // Start the various services

            Log.i(TAG, "Loading app settings");
            loadPreferences(appContext);

            Log.i(TAG, "Starting key value store");
            KVStore.start(appContext);

            Log.i(TAG, "Starting bluetooth service");
            if(BluetoothService.preferenceEnabled) {
                BluetoothService.start(activity);
            }

            Log.i(TAG, "Starting location service");
            if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Enable location services
                try {
                    location = new LocationService();
                    location.start(appContext);
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
            Log.v(TAG, "Services already started");
        }

        // TODO: Upload any unsynced files
        // TheCloud.uploadAll();
    }

    /**
     * Call this function once text-to-speech data is ready
     */
    public static void onTtsLoaded(Context appContext) {
        // TTS loaded, start the audible
        MyAudible.init(appContext);
    }

    public static void stop() {
        startCount--;
        if(startCount == 0) {
            handler.postDelayed(stopRunnable, shutdownDelay);
        }
    }

    /**
     * A thread that shuts down services after activity has stopped
     */
    private static final Runnable stopRunnable = new Runnable() {
        @Override
        public void run() {
            if(initialized && startCount == 0) {
                if(!MyDatabase.isLogging() && !MyAudible.isEnabled()) {
                    Log.i(TAG, "All activities have stopped. Stopping services.");
                    // Stop services
                    MyAudible.terminate();
                    MyAltimeter.stop();
                    location.stop();
                    location = null;
                    KVStore.stop();
                    initialized = false;
                } else {
                    if(MyDatabase.isLogging()) {
                        Log.w(TAG, "All activities have stopped, but still recording track. Leaving services running.");
                    }
                    if(MyAudible.isEnabled()) {
                        Log.w(TAG, "All activities have stopped, but audible still active. Leaving services running.");
                    }
                }
            }
        }
    };

    private static void loadPreferences(Context appContext) {
        final SharedPreferences prefs =  PreferenceManager.getDefaultSharedPreferences(appContext);

        // Metric
        Convert.metric = prefs.getBoolean("metric_enabled", false);

        // Bluetooth
        BluetoothService.preferenceEnabled = prefs.getBoolean("bluetooth_enabled", false);
        BluetoothService.preferenceDevice = prefs.getString("bluetooth_device", null);

        // Home location
        final double home_latitude = Util.parseDouble(prefs.getString("home_latitude", null));
        final double home_longitude = Util.parseDouble(prefs.getString("home_longitude", null));
        if(Util.isReal(home_latitude) && Util.isReal(home_longitude)) {
            // Set home location
            MyFlightManager.homeLoc = new LatLng(home_latitude, home_longitude);
        }
    }

}
