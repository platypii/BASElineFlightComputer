package com.platypii.baseline;

import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.bluetooth.BluetoothService;
import com.platypii.baseline.location.LocationServiceBlue;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.views.BaseActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

/**
 * Start and stop essential services.
 * This class provides essential services intended to persist between activities.
 * This class will also keep services running if logging or audible is enabled.
 */
public class Services {
    private static final String TAG = "Services";

    // Count the number of times an activity has started.
    // This allows us to only stop services once the app is really done.
    private static int startCount = 0;
    private static boolean initialized = false;

    // How long to wait after the last activity shutdown to terminate services
    private final static Handler handler = new Handler();
    private static final int shutdownDelay = 10000;

    // Services
    public static SharedPreferences prefs;
    public static final BluetoothService bluetooth = new BluetoothService();
    public static final LocationServiceBlue location = new LocationServiceBlue(bluetooth);
    public static final MyAltimeter alti = location.alti;

    /**
     * We want preferences to be available as early as possible.
     * Call this in onCreate
     */
    public static void create(@NonNull Activity activity) {
        if(prefs == null) {
            Log.i(TAG, "Loading app preferences");
            loadPreferences(activity.getApplicationContext());
        }
    }

    public static void start(@NonNull Activity activity) {
        startCount++;
        if(!initialized) {
            initialized = true;
            final long startTime = System.currentTimeMillis();
            Log.i(TAG, "Starting services");
            final Context appContext = activity.getApplicationContext();
            handler.removeCallbacks(stopRunnable);

            // Start the various services

            Log.i(TAG, "Starting bluetooth service");
            if(bluetooth.preferences.preferenceEnabled) {
                bluetooth.start(activity);
            }

            Log.i(TAG, "Starting location service");
            // Note: Activity.checkSelfPermission added in minsdk 23
            if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Enable location services
                try {
                    location.start(appContext);
                } catch (SecurityException e) {
                    Log.e(TAG, "Failed to start location service", e);
                }
            } else {
                // Request the missing permissions
                final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
                ActivityCompat.requestPermissions(activity, permissions, BaseActivity.RC_LOCATION);
            }

            Log.i(TAG, "Starting altimeter");
            alti.start(appContext);

            Log.i(TAG, "Services started in " + (System.currentTimeMillis() - startTime) + " ms");
        } else if(startCount > 2) {
            // Activity lifecycles can overlap
            Log.w(TAG, "Services started more than twice");
        } else {
            Log.v(TAG, "Services already started");
        }
    }

    public static void stop() {
        startCount--;
        if(startCount == 0) {
            Log.i(TAG, String.format("All activities have stopped. Services will stop in %.3fs", shutdownDelay * 0.001));
            handler.postDelayed(stopRunnable, shutdownDelay);
        }
    }

    /**
     * A thread that shuts down services after activity has stopped
     */
    private static final Runnable stopRunnable = Services::stopIfIdle;

    /**
     * Stop services IF nothing is using them
     */
    private static synchronized void stopIfIdle() {
        if(initialized && startCount == 0) {
            Log.i(TAG, "All activities have stopped. Stopping services.");
            // Stop services
            alti.stop();
            location.stop();
            bluetooth.stop();
            initialized = false;
            handler.removeCallbacks(stopRunnable);
        }
    }

    private static void loadPreferences(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // Metric
        Convert.metric = prefs.getBoolean("metric_enabled", false);

        // Bluetooth
        bluetooth.preferences.load(prefs);
    }

}