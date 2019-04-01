package com.platypii.baseline;

import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.audible.CheckTextToSpeechTask;
import com.platypii.baseline.audible.MyAudible;
import com.platypii.baseline.bluetooth.BluetoothService;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.cloud.BaselineCloud;
import com.platypii.baseline.cloud.tasks.Tasks;
import com.platypii.baseline.jarvis.AutoStop;
import com.platypii.baseline.jarvis.FlightComputer;
import com.platypii.baseline.location.LandingZone;
import com.platypii.baseline.location.LocationService;
import com.platypii.baseline.places.Places;
import com.platypii.baseline.sensors.MySensorManager;
import com.platypii.baseline.tracks.TrackLogger;
import com.platypii.baseline.tracks.TrackStore;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.util.Numbers;
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
import com.google.android.gms.maps.model.LatLng;

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

    /** A handler to shut down services after activity has stopped */
    private static final Handler handler = new Handler();
    private static final int shutdownDelay = 10000;
    private static final Runnable stopRunnable = Services::stopIfIdle;

    // Have we checked for TTS data?
    private static boolean ttsLoaded = false;

    // Services
    public static final TrackLogger logger = new TrackLogger();
    public static final TrackStore trackStore = new TrackStore();
    public static final BluetoothService bluetooth = new BluetoothService();
    public static final LocationService location = new LocationService(bluetooth);
    public static final MyAltimeter alti = location.alti;
    public static final MySensorManager sensors = new MySensorManager();
    public static final FlightComputer flightComputer = new FlightComputer();
    public static final MyAudible audible = new MyAudible();
    private static final Notifications notifications = new Notifications();
    public static final Tasks tasks = new Tasks();
    public static final BaselineCloud cloud = new BaselineCloud();
    public static final Places places = new Places();

    /**
     * We want preferences to be available as early as possible.
     * Call this in onCreate
     */
    public static void create(@NonNull Activity activity) {
        if (!created) {
            Log.i(TAG, "Loading app preferences");
            loadPreferences(activity);
            created = true;
        }
    }
    private static boolean created = false;

    public static void start(@NonNull Activity activity) {
        final boolean shouldStart = inc();
        if (shouldStart && initialized) {
            // This happens when services are started again before the shutdown delay
            Log.i(TAG, "Services still alive");
            // Even without this line, stopRunnable would notice that startCount > 0.
            // But why waste the cycles? Might as well remove the stop runnable.
            handler.removeCallbacks(stopRunnable);
        } else if (shouldStart) {
            initialized = true;
            final long startTime = System.currentTimeMillis();
            Log.i(TAG, "Starting services");
            final Context appContext = activity.getApplicationContext();

            // Start the various services

            Log.i(TAG, "Starting bluetooth service");
            if (bluetooth.preferences.preferenceEnabled) {
                bluetooth.start(activity);
            }

            Log.i(TAG, "Starting track store service");
            trackStore.start(appContext);

            Log.i(TAG, "Starting logger service");
            logger.start(appContext);

            Log.i(TAG, "Starting location service");
            // Note: Activity.checkSelfPermission added in minsdk 23
            if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                // Enable location services
                location.start(appContext);
            } else {
                // Request the missing permissions
                final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
                ActivityCompat.requestPermissions(activity, permissions, BaseActivity.RC_LOCATION);
            }

            Log.i(TAG, "Starting sensors");
            sensors.start(appContext);

            Log.i(TAG, "Starting altimeter");
            alti.start(appContext);

            Log.i(TAG, "Starting flight services");
            flightComputer.start(appContext);

            // TTS is prerequisite for audible
            if (ttsLoaded) {
                Log.i(TAG, "Text-to-speech data already loaded, starting audible");
                audible.start(appContext);
            } else {
                Log.i(TAG, "Checking for text-to-speech data");
                new CheckTextToSpeechTask(activity).execute();
            }

            Log.i(TAG, "Starting notification bar service");
            notifications.start(appContext);

            Log.i(TAG, "Starting task manager");
            tasks.start(appContext);

            Log.i(TAG, "Starting cloud services");
            cloud.start(appContext);

            Log.i(TAG, "Starting place database");
//            places.start(appContext);

            Log.i(TAG, "Services started in " + (System.currentTimeMillis() - startTime) + " ms");
        }
    }

    /**
     * BaseActivity calls this function once text-to-speech data is ready
     */
    public static void onTtsLoaded(@NonNull Activity context) {
        // TTS loaded, start the audible
        Exceptions.log("onTtsLoaded from " + context.getLocalClassName());
        if (!ttsLoaded) {
            Log.i(TAG, "Text-to-speech data loaded, starting audible");
            ttsLoaded = true;
            if (initialized) {
                audible.start(context.getApplicationContext());
            }
        } else {
            Log.w(TAG, "Text-to-speech already loaded");
            Exceptions.report(new IllegalStateException("Text-to-speech loaded twice"));
        }
    }

    /**
     * Increment startCount, and return true if 0 -> 1, meaning start services
     */
    private static synchronized boolean inc() {
        return startCount++ == 0;
    }
    /**
     * Decrement startCount, and return true if 1 -> 0, meaning stop services
     */
    private static synchronized boolean dec() {
        return --startCount == 0;
    }

    public static void stop() {
        if (dec()) {
            Log.i(TAG, String.format("All activities have stopped. Base services will stop in %.3fs", shutdownDelay * 0.001));
            handler.postDelayed(stopRunnable, shutdownDelay);
        }
    }

    /**
     * Stop services IF nothing is using them
     */
    private static synchronized void stopIfIdle() {
        if (initialized && startCount == 0) {
            if (!logger.isLogging() && !audible.isEnabled()) {
                Log.i(TAG, "All activities have stopped. Stopping services.");
                // Stop services
                places.stop();
                cloud.stop();
                tasks.stop();
                notifications.stop();
                audible.stop();
                flightComputer.stop();
                alti.stop();
                sensors.stop();
                location.stop();
                logger.stop();
                trackStore.stop();
                bluetooth.stop();
                initialized = false;
            } else {
                if (logger.isLogging()) {
                    Log.w(TAG, "All activities have stopped, but still recording track. Leaving services running.");
                }
                if (audible.isEnabled()) {
                    Log.w(TAG, "All activities have stopped, but audible still active. Leaving services running.");
                }
                // Try again periodically
                handler.postDelayed(stopRunnable, shutdownDelay);
            }
        }
    }

    private static void loadPreferences(@NonNull Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // Metric
        Convert.metric = prefs.getBoolean("metric_enabled", Convert.metric);

        // Barometer
        Services.alti.barometerEnabled = prefs.getBoolean("barometer_enabled", true);

        // Auto-stop
        AutoStop.preferenceEnabled = prefs.getBoolean("auto_stop_enabled", true);

        // Sign in state
        AuthState.loadFromPreferences(context);

        // Bluetooth
        bluetooth.preferences.load(context);

        // Home location
        final double home_latitude = Numbers.parseDouble(prefs.getString("home_latitude", null));
        final double home_longitude = Numbers.parseDouble(prefs.getString("home_longitude", null));
        if (Numbers.isReal(home_latitude) && Numbers.isReal(home_longitude)) {
            // Set home location
            LandingZone.homeLoc = new LatLng(home_latitude, home_longitude);
        }
    }

}
