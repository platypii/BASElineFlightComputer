package com.platypii.baseline.jarvis;

import com.platypii.baseline.Services;
import com.platypii.baseline.measurements.MLocation;
import android.util.Log;

class AutoStop {
    private static final String TAG = "AutoStop";

    public static boolean preferenceEnabled = true;

    private static final int STATE_STOPPED = 0;
    private static final int STATE_STARTED = 1;
    private static final int STATE_EXITED = 2;
    private static final int STATE_LANDED = 3;

    private static final double minHeight = 60;

    private int state = STATE_STOPPED;

    private double prExited = 0;
    private double prLanded = 0;

    private double altMin = Double.NaN;
    private double altMax = Double.NaN;

    void update(MLocation loc) {
        final double alt = loc.altitude_gps;
        // Update altitude range
        if(!Double.isNaN(alt)) {
            if(!(altMin < alt)) altMin = alt;
            if(!(alt < altMax)) altMax = alt;
        }
        // Update state
        if(state == STATE_STARTED) {
            // Look for flight / freefall
            if(loc.climb < -15 && altMax - alt > minHeight) {
                prExited += (1 - prExited) * 0.6;
            } else if(loc.flightMode() == FlightMode.MODE_CANOPY && altMax - alt > minHeight) {
                prExited += (1 - prExited) * 0.6;
            } else {
                prExited -= prExited * 0.6;
            }
            if(prExited > 0.85) {
                exited();
            }
        } else if(state == STATE_EXITED) {
            // Look for landing
            final double altNormalized = (alt - altMin) / (altMax - altMin);
            if(loc.flightMode() == FlightMode.MODE_GROUND && altMax - altMin > minHeight && altNormalized < 0.1) {
                prLanded += (1 - prExited) * 0.6;
            }
            if(prLanded > 0.95) {
                landed();
            }
        }
    }

    /**
     * Called when logging is started
     */
    void startLogging() {
        // Reset state
        state = STATE_STARTED;
        prExited = 0;
        prLanded = 0;
        // TODO: Should we reset altitude range per recording or per app session?
        altMin = Double.NaN;
        altMax = Double.NaN;
    }

    /**
     * Called when logging is stopped by the user
     */
    void stopLogging() {
        state = STATE_STOPPED;
    }

    private void exited() {
        Log.i(TAG, "Exit detected");
        state = STATE_EXITED;
        Services.audible.speakNow("Exit"); // TODO: Say exit for debug only
    }
    private void landed() {
        Log.i(TAG, "Landing detected");
        state = STATE_LANDED;
        // If audible enabled, say landing detected
        Services.audible.speakNow("Landing detected");
        if(preferenceEnabled) {
            // If audible enabled, disable
            Services.audible.disableAudible();
            // If logging enabled, disable
            Services.logger.stopLogging();
            // TODO: Returns a trackfile, upload to cloud?
        }
    }
}
