package com.platypii.baseline.audible;

import android.app.Activity;

import com.platypii.baseline.Services;
import com.platypii.baseline.events.AudibleEvent;
import com.platypii.baseline.jarvis.FlightMode;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.util.Numbers;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.greenrobot.eventbus.EventBus;

/**
 * Periodically gives audio feedback
 */
public class MyAudible {
    private static final String TAG = "Audible";

    public final AudibleSettings settings = new AudibleSettings();
    private SharedPreferences prefs;

    @NonNull
    private final Speech speech = new Speech();
    @Nullable
    private AudibleThread audibleThread;

    // Audible service initialized, but does not mean that speech is ready
    private boolean isInitialized = false;

    // When was the last time we announced airplane mode?
    private long airplaneAnnounceTime;
    private static final long AIRPLANE_ANNOUNCE_INTERVAL = 30000; // 30 seconds

    // Was the last sample below/inside/above the boundary?
    private static final int STATE_MIN = -1;
    private static final int STATE_INSIDE = 0;
    private static final int STATE_MAX = 1;
    private int boundaryState = STATE_INSIDE;

    public void start(@NonNull Activity activity) {
        Log.i(TAG, "Initializing audible");
        if (!isInitialized) {
            prefs = PreferenceManager.getDefaultSharedPreferences(activity);
            settings.load(prefs);
            // Audible thread has a handler, which needs to be created in looper thread
            audibleThread = new AudibleThread();
            if (settings.isEnabled) {
                enableAudible(activity);
            }
            isInitialized = true;
        } else {
            Log.w(TAG, "Audible initialized twice");
            Exceptions.report(new IllegalStateException("Audible initialized twice"));
        }
    }

    public void enableAudible(@NonNull Activity activity) {
        Log.i(TAG, "Starting audible");
        speech.start(activity);
        if (audibleThread == null) {
            Log.e(TAG, "Audible thread should not be null");
        } else if (audibleThread.isRunning()) {
            Log.w(TAG, "Audible thread already started");
        } else {
            boundaryState = STATE_INSIDE;
            audibleThread.start();
        }

        // Say audible mode
        speakModeWhenReady();
        // Play first measurement
        speakWhenReady();

        // Save to preferences
        settings.isEnabled = true;
        if (prefs != null) {
            final SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("audible_enabled", true);
            editor.apply();
        }
        EventBus.getDefault().post(new AudibleEvent(true));
    }

    public void disableAudible() {
        if (isInitialized) {
            speech.stopAll();
            audibleThread.stop();
            speech.speakWhenReady("Goodbye");
        } else {
            Log.e(TAG, "Failed to stop audible: audible not initialized");
        }

        // Save to preferences
        settings.isEnabled = false;
        if (prefs != null) {
            final SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean("audible_enabled", false);
            editor.apply();
        }
        EventBus.getDefault().post(new AudibleEvent(false));
    }

    /**
     * BaseActivity calls this function once text-to-speech data is ready
     */
    public void onTtsLoaded(@NonNull Context context) {
        speech.onTtsLoaded(context);
    }

    /**
     * Announce the current audible mode
     */
    private void speakModeWhenReady() {
        speech.speakWhenReady(settings.mode.name);
    }

    /**
     * Make a special announcement
     */
    public void speakNow(String text) {
        if (!settings.isEnabled) {
            Log.e(TAG, "Should never speak when audible is disabled");
            Exceptions.report(new IllegalStateException("MyAudible.speakNow should never speak when audible is disabled"));
        }
        speech.speakNow(text);
    }

    void speak() {
        final String measurement = getMeasurement();
        if (!measurement.isEmpty()) {
            speech.speakNow(measurement);
        }
    }

    private void speakWhenReady() {
        final String measurement = getMeasurement();
        if (!measurement.isEmpty()) {
            speech.speakWhenReady(measurement);
        }
    }

    /**
     * Returns the text of what to say for the current measurement mode.
     * This function mostly just handles airplane mode checks.
     */
    @NonNull
    private String getMeasurement() {
        // First, check for airplane mode
        if (settings.airplaneMode && Services.flightComputer.flightMode == FlightMode.MODE_PLANE) {
            // Announce every N seconds
            final long delta = System.currentTimeMillis() - airplaneAnnounceTime;
            if (AIRPLANE_ANNOUNCE_INTERVAL < delta) {
                // Announce airplane mode
                Log.i(TAG, "Airplane mode");
                airplaneAnnounceTime = System.currentTimeMillis();
                return "Airplane mode";
            } else {
                return "";
            }
        } else {
            // Not airplane mode
            airplaneAnnounceTime = 0;
            return getMeasurementSample();
        }
    }

    /**
     * Get the text of what to say for the current measurement value
     */
    @NonNull
    private String getMeasurementSample() {
        final AudibleSample sample = settings.mode.currentSample(settings.precision);
        // Check for fresh signal (not applicable to vertical speed)
        if (settings.mode.id.equals("vertical_speed") || goodGpsFix()) {
            // Check for real valued sample
            if (Numbers.isReal(sample.value)) {
                if (sample.value < settings.min) {
                    if (boundaryState != STATE_MIN) {
                        boundaryState = STATE_MIN;
                        return "min";
                    } else {
                        Log.i(TAG, "Not speaking: min, mode = " + settings.mode.id + " sample = " + sample);
                        return "";
                    }
                } else if (settings.max < sample.value) {
                    if (boundaryState != STATE_MAX) {
                        boundaryState = STATE_MAX;
                        return "max";
                    } else {
                        Log.i(TAG, "Not speaking: max, mode = " + settings.mode.id + " sample = " + sample);
                        return "";
                    }
                } else {
                    boundaryState = STATE_INSIDE;
                    return sample.phrase;
                }
            } else {
                Log.w(TAG, "Not speaking: no signal, mode = " + settings.mode.id + " sample = " + sample);
                return "";
            }
        } else {
            Log.w(TAG, "Not speaking: stale signal, mode = " + settings.mode.id + " sample = " + sample);
            return "";
        }
    }

    /**
     * Return true if GPS signal is fresh
     */
    private boolean goodGpsFix() {
        if (Services.location.lastLoc != null && Services.location.lastFixDuration() < 3500) {
            gpsFix = true;
        } else {
            if (Services.location.lastLoc == null) {
                Log.w(TAG, "No GPS signal");
            } else {
                Log.w(TAG, "Stale GPS signal");
            }
            if (gpsFix) {
                speech.speakNow("Signal lost");
            }
            gpsFix = false;
        }
        return gpsFix;
    }

    /**
     * True iff the last measurement was a good fix
     */
    private boolean gpsFix = false;

    /**
     * Stop audible service
     */
    public void stop() {
        if (isInitialized) {
            if (settings.isEnabled) {
                disableAudible();
            }
            audibleThread = null;
            isInitialized = false;
        }
    }

}
