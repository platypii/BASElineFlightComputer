package com.platypii.baseline.jarvis;

import com.platypii.baseline.Services;
import com.platypii.baseline.measurements.MLocation;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

/**
 * Automatically stop logging when landing is detected (after a known jump)
 */
public class AutoStop {
    private static final String TAG = "AutoStop";

    public static boolean preferenceEnabled = true;

    // Jump detection state
    private static final int STATE_STOPPED = 0;
    private static final int STATE_STARTED = 1;
    private static final int STATE_EXITED = 2;
    private int state = STATE_STOPPED;

    // When auto stop is enabled, if we haven't detected landing in 1 hour, stop recording
    private final static Handler handler = new Handler();
    private static final long autoTimeout = 3600000; // 1 hour

    private static final double minHeight = 60;

    private double prExited = 0;
    private double prLanded = 0;

    // Stats
    private long lastMillis = 0;
    private double altMin = Double.NaN;
    private double altMax = Double.NaN;

    void update(@NonNull MLocation loc) {
        if (loc.millis - lastMillis >= 1000) {
            lastMillis = loc.millis;
            update1hz(loc);
        }
    }

    /**
     * Update to be called at most once per second
     */
    private void update1hz(@NonNull MLocation loc) {
        final double alt = loc.altitude_gps;
        // Update altitude range
        if (!Double.isNaN(alt)) {
            if (!(altMin < alt)) altMin = alt;
            if (!(alt < altMax)) altMax = alt;
        }
        // Update state
        if (state == STATE_STARTED) {
            // Look for flight / freefall
            if (loc.climb < -15 && altMax - alt > minHeight) {
                prExited += (1 - prExited) * 0.6;
            } else if (FlightMode.getMode(loc) == FlightMode.MODE_CANOPY && altMax - alt > minHeight) {
                prExited += (1 - prExited) * 0.2;
            } else {
                prExited -= prExited * 0.6;
            }
            if (prExited > 0.85) {
                Log.i(TAG, "Exit detected");
                state = STATE_EXITED;
            }
        } else if (state == STATE_EXITED) {
            // Look for landing
            final double altNormalized = (alt - altMin) / (altMax - altMin);
            if (FlightMode.getMode(loc) == FlightMode.MODE_GROUND && altMax - altMin > minHeight && altNormalized < 0.1) {
                prLanded += (1 - prLanded) * 0.2;
            }
            if (prLanded > 0.99) {
                Log.i(TAG, "Landing detected");
                landed();
            }
        }
    }

    void start() {
        if (state == STATE_STOPPED) {
            // Reset state
            state = STATE_STARTED;
            prExited = 0;
            prLanded = 0;
            // TODO: Should we reset altitude range per recording or per app session?
            altMin = Double.NaN;
            altMax = Double.NaN;
            // When auto stop is enabled, timeout after 1 hour
            handler.postDelayed(stopRunnable, autoTimeout);
        } else {
            Log.e(TAG, "Autostop should not be started twice");
        }
    }

    void stop() {
        if (state != STATE_STOPPED) {
            state = STATE_STOPPED;
            // Stop timeout thread
            handler.removeCallbacks(stopRunnable);
        } else {
            Log.e(TAG, "Autostop should not be stopped twice");
        }
    }

    private void landed() {
        state = STATE_STOPPED;
        // If audible enabled, say landing detected
        if (preferenceEnabled) {
            // If audible enabled, disable
            if (Services.audible.isEnabled()) {
                Services.audible.disableAudible();
            }
            // If logging enabled, disable
            if (Services.logger.isLogging()) {
                Services.logger.stopLogging();
            } else {
                Log.e(TAG, "Landing detected, but logger not logging");
            }
        }
    }

    /**
     * A thread that stops recording after 1 hour
     */
    private final Runnable stopRunnable = () -> {
        Log.i(TAG, "Auto-stop timeout");
        landed();
    };

}
