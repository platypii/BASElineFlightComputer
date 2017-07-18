package com.platypii.baseline.altimeter;

import android.content.SharedPreferences;
import android.util.Log;
import com.google.firebase.crash.FirebaseCrash;
import com.platypii.baseline.measurements.MPressure;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Numbers;

/**
 * Manages ground level.
 * Barometer provides pressure altitude, GPS provides AMSL.
 * This class records the ground level as pressure altitude, because the barometer is more reliable.
 */
class GroundLevel {
    private static final String TAG = "GroundLevel";

    private SharedPreferences prefs;

    // Ground level
    // Save ground level for 12 hours (in milliseconds)
    private static final long GROUND_LEVEL_TTL = 12 * 60 * 60 * 1000;
    boolean isInitialized = false;
    double ground_level = Double.NaN;

    private long baro_sample_count = 0;

    /**
     * Load ground level from preferences
     */
    void start(SharedPreferences prefs) {
        this.prefs = prefs;
        // Load ground level from preferences
        final long groundLevelTime = prefs.getLong("altimeter_ground_level_time", -1L);
        if(groundLevelTime != -1 && System.currentTimeMillis() - groundLevelTime < GROUND_LEVEL_TTL) {
            ground_level = prefs.getFloat("altimeter_ground_level", Float.NaN);
            if(Numbers.isReal(ground_level)) {
                isInitialized = true;
                Log.i(TAG, "Restoring ground level from preferences: " + Convert.distance(ground_level, 2, true));
            }
        }
    }

    /**
     * Called by parent MyAltimeter
     */
    void onPressureEvent(MPressure pressure) {
        if(Numbers.isReal(pressure.altitude)) {
            if (!isInitialized) {
                if (baro_sample_count == 0) {
                    // First pressure reading. Calibrate ground level.
                    ground_level = pressure.altitude;
                } else if (baro_sample_count < 30) {
                    // Average the first N raw samples
                    ground_level += (pressure.altitude - ground_level) / (baro_sample_count + 1);
                } else {
                    setGroundLevel(ground_level);
                }
            }
            baro_sample_count++;
        }
    }

    /**
     * Set ground level, based on pressure altitude.
     * Should not be called until altimeter is initialized.
     * @param groundLevel the pressure altitude at ground level (0m AGL)
     */
    void setGroundLevel(double groundLevel) {
        if(Numbers.isReal(groundLevel)) {
            ground_level = groundLevel;
            isInitialized = true;
            if(prefs != null) {
                // Save to preferences
                final SharedPreferences.Editor edit = prefs.edit();
                edit.putFloat("altimeter_ground_level", (float) ground_level);
                edit.putLong("altimeter_ground_level_time", System.currentTimeMillis());
                edit.apply();
            }
        } else {
            FirebaseCrash.report(new IllegalArgumentException("Ground level must be real: " + groundLevel));
        }
    }

}
