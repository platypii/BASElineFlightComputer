package com.platypii.baseline;

import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.audible.CheckTextToSpeechTask;
import com.platypii.baseline.audible.MyAudible;
import com.platypii.baseline.bluetooth.BluetoothService;
import com.platypii.baseline.jarvis.FlightComputer;
import com.platypii.baseline.location.LandingZone;
import com.platypii.baseline.location.LocationService;
import com.platypii.baseline.sensors.MySensorManager;
import com.platypii.baseline.tracks.MigrateTracks;
import com.platypii.baseline.tracks.TrackLogger;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Numbers;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.crash.FirebaseCrash;

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

    // Have we checked for TTS data?
    private static boolean ttsLoaded = false;

    // Services
    public static SharedPreferences prefs;
    public static final TrackLogger logger = new TrackLogger();
    public static final LocationService location = new LocationService();
    public static final MyAltimeter alti = new MyAltimeter();
    public static final MySensorManager sensors = new MySensorManager();
    static final FlightComputer flightComputer = new FlightComputer();
    public static final BluetoothService bluetooth = new BluetoothService();
    public static final MyAudible audible = new MyAudible();
    private static final Notifications notifications = new Notifications();

    /**
     * We want preferences to be available as early as possible.
     * Call this in onCreate
     */
    static void create(@NonNull Activity activity) {
        if(prefs == null) {
            Log.i(TAG, "Loading app preferences");
            loadPreferences(activity.getApplicationContext());
        }
    }

    static void start(@NonNull Activity activity) {
        startCount++;
        if(!initialized) {
            initialized = true;
            Log.i(TAG, "Starting services");
            final Context appContext = activity.getApplicationContext();
            handler.removeCallbacks(stopRunnable);

            // Start the various services

            Log.i(TAG, "Starting bluetooth service");
            if(BluetoothService.preferenceEnabled) {
                bluetooth.start(activity);
            }

            // Initialize track logger
            logger.start(appContext);

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
                ActivityCompat.requestPermissions(activity, permissions, Intents.RC_LOCATION);
            }

            Log.i(TAG, "Starting sensors");
            sensors.start(appContext);

            Log.i(TAG, "Starting altimeter");
            alti.start(appContext);

            Log.i(TAG, "Starting flight services");
            flightComputer.start(appContext);

            // TTS is prerequisite for audible
            if(ttsLoaded) {
                Log.i(TAG, "Text-to-speech data already loaded, starting audible");
                FirebaseCrash.log("text-to-speech already loaded");
                audible.start(appContext);
            } else {
                Log.i(TAG, "Checking for text-to-speech data");
                new CheckTextToSpeechTask(activity).execute();
            }

            Log.i(TAG, "Starting notification bar service");
            notifications.start(appContext);

            // Check if migration is necessary
            MigrateTracks.migrate(appContext);

            Log.i(TAG, "Services started");
        } else if(startCount > 2) {
            // Activity lifecycles can overlap
            Log.w(TAG, "Services started more than twice");
        } else {
            Log.v(TAG, "Services already started");
        }

        // TODO: If you wanted to automatically upload any unsynced files, this is how:
        // BaselineCloud.uploadAll();
    }

    /**
     * BaseActivity calls this function once text-to-speech data is ready
     */
    static void onTtsLoaded(Context context) {
        // TTS loaded, start the audible
        ttsLoaded = true;
        FirebaseCrash.log("onTtsLoaded");
        audible.start(context);
    }

    static void stop() {
        startCount--;
        if(startCount == 0) {
            Log.i(TAG, String.format("All activities have stopped. Services will stop in %.3fs", shutdownDelay * 0.001));
            handler.postDelayed(stopRunnable, shutdownDelay);
        }
    }

    /**
     * A thread that shuts down services after activity has stopped
     */
    private static final Runnable stopRunnable = new Runnable() {
        @Override
        public void run() {
            stopIfIdle();
        }
    };

    /**
     * Stop services IF nothing is using them
     */
    private static synchronized void stopIfIdle() {
        if(initialized && startCount == 0) {
            if(!logger.isLogging() && !audible.isEnabled()) {
                Log.i(TAG, "All activities have stopped. Stopping services.");
                // Stop services
                notifications.stop();
                audible.stop();
                flightComputer.stop();
                alti.stop();
                sensors.stop();
                location.stop();
                logger.stop();
                bluetooth.stop();
                initialized = false;
                handler.removeCallbacks(stopRunnable);
            } else {
                if(logger.isLogging()) {
                    Log.w(TAG, "All activities have stopped, but still recording track. Leaving services running.");
                }
                if(audible.isEnabled()) {
                    Log.w(TAG, "All activities have stopped, but audible still active. Leaving services running.");
                }
            }
        }
    }

    private static void loadPreferences(Context context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // Metric
        Convert.metric = prefs.getBoolean("metric_enabled", false);

        // Bluetooth
        BluetoothService.preferenceEnabled = prefs.getBoolean("bluetooth_enabled", false);
        BluetoothService.preferenceDeviceId = prefs.getString("bluetooth_device_id", null);
        BluetoothService.preferenceDeviceName = prefs.getString("bluetooth_device_name", null);

        // Home location
        final double home_latitude = Numbers.parseDouble(prefs.getString("home_latitude", null));
        final double home_longitude = Numbers.parseDouble(prefs.getString("home_longitude", null));
        if(Numbers.isReal(home_latitude) && Numbers.isReal(home_longitude)) {
            // Set home location
            LandingZone.homeLoc = new LatLng(home_latitude, home_longitude);
        }
    }

}
