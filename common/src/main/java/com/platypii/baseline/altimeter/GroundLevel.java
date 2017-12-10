package com.platypii.baseline.altimeter;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.measurements.MPressure;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.util.Numbers;

/**
 * Manages ground level.
 * Barometer provides pressure altitude, GPS provides AMSL.
 * If a barometer is available, we use that, because it is not susceptible to random gaps or
 * discontinuities in altitude. We also track the ground level relative to GPS, as a fallback.
 */
public class GroundLevel {
    private static final String TAG = "GroundLevel";

    // Save ground level for 12 hours (in milliseconds)
    private static final long GROUND_LEVEL_TTL = 12 * 60 * 60 * 1000;

    private SharedPreferences prefs;

    // Barometer ground level
    private boolean baroInitialized = false;
    private long baro_sample_count = 0;
    private double ground_pressure_altitude = Double.NaN;
    private double pressure_altitude = Double.NaN;

    // GPS ground level
    private boolean gpsInitialized = false;
    private double ground_altitude_msl = Double.NaN;
    private double altitude_msl = Double.NaN;

    /**
     * Load ground level from preferences
     */
    void start(SharedPreferences prefs) {
        // Load ground level from preferences
        final long groundLevelTime = prefs.getLong("altimeter.groundlevel.time", -1L);
        if(groundLevelTime != -1 && System.currentTimeMillis() - groundLevelTime < GROUND_LEVEL_TTL) {
            ground_pressure_altitude = prefs.getFloat("altimeter.groundlevel.pressure_altitude", Float.NaN);
            ground_altitude_msl = prefs.getFloat("altimeter.groundlevel.altitude_msl", Float.NaN);
            if(Numbers.isReal(ground_pressure_altitude)) {
                baroInitialized = true;
                Log.i(TAG, "Restored ground pressure altitude from preferences: " + Convert.distance(ground_pressure_altitude, 2, true));
            }
            if(Numbers.isReal(ground_altitude_msl)) {
                gpsInitialized = true;
                Log.i(TAG, "Restored ground altitude msl from preferences: " + Convert.distance(ground_altitude_msl, 2, true));
            }
        }
        this.prefs = prefs;
    }

    double altitudeAGL() {
        if(baroInitialized && !Double.isNaN(pressure_altitude)) {
            return pressure_altitude - ground_pressure_altitude;
        } else if(gpsInitialized && !Double.isNaN(altitude_msl)) {
            return altitude_msl - ground_altitude_msl;
        } else {
            return Double.NaN;
        }
    }

    /**
     * Called by parent MyAltimeter
     */
    void onPressureEvent(@NonNull MPressure pressure) {
        if(Double.isNaN(pressure.altitude)) return;
        pressure_altitude = pressure.altitude;
        if (!baroInitialized) {
            if (baro_sample_count == 0) {
                // First pressure reading. Calibrate ground level.
                ground_pressure_altitude = pressure_altitude;
            } else if (baro_sample_count < 30) {
                // Average the first N raw samples
                ground_pressure_altitude += (pressure_altitude - ground_pressure_altitude) / (baro_sample_count + 1);
            } else {
                saveGroundPressureAltitude();
            }
        }
        baro_sample_count++;
    }

    /**
     * Called by parent MyAltimeter
     */
    void onLocationEvent(@NonNull MLocation loc) {
        if(Double.isNaN(loc.altitude_gps)) return;
        altitude_msl = loc.altitude_gps;
        if (!gpsInitialized) {
            if(baroInitialized) {
                // Set based on current altitude AGL
                final double altitude_agl = pressure_altitude - ground_pressure_altitude;
                ground_altitude_msl = altitude_msl - altitude_agl;
            } else {
                // Probably GPS only
                ground_altitude_msl = loc.altitude_gps;
            }
            saveGroundAltitudeMSL();
        }
    }

    /**
     * Set ground level for both baro and gps based on a specified altitude agl
     */
    public void setCurrentAltitudeAGL(double altitude_agl) {
        if(baroInitialized && !Double.isNaN(pressure_altitude)) {
            ground_pressure_altitude = pressure_altitude - altitude_agl;
            saveGroundPressureAltitude();
        }
        if(gpsInitialized && !Double.isNaN(altitude_msl)) {
            ground_altitude_msl = altitude_msl - altitude_agl;
            saveGroundAltitudeMSL();
        }
    }

    /**
     * Set ground level, based on pressure altitude.
     * Should not be called until altimeter is initialized.
     */
    private void saveGroundPressureAltitude() {
        if(Numbers.isReal(ground_pressure_altitude)) {
            baroInitialized = true;
            if(prefs != null) {
                // Save to preferences
                final SharedPreferences.Editor edit = prefs.edit();
                edit.putFloat("altimeter.groundlevel.pressure_altitude", (float) ground_pressure_altitude);
                edit.putLong("altimeter.groundlevel.time", System.currentTimeMillis());
                edit.apply();
            } else {
                Log.e(TAG, "Preferences should not be null");
            }
        } else {
            Exceptions.report(new IllegalArgumentException("Ground pressure altitude must be real: " + ground_pressure_altitude));
        }
    }

    /**
     * Set ground level, based on pressure altitude.
     * Should not be called until altimeter is initialized.
     */
    private void saveGroundAltitudeMSL() {
        if(Numbers.isReal(ground_altitude_msl)) {
            gpsInitialized = true;
            if(prefs != null) {
                // Save to preferences
                final SharedPreferences.Editor edit = prefs.edit();
                edit.putFloat("altimeter.groundlevel.altitude_msl", (float) ground_altitude_msl);
                edit.putLong("altimeter.groundlevel.time", System.currentTimeMillis());
                edit.apply();
            } else {
                Log.e(TAG, "Preferences should not be null");
            }
        } else {
            Exceptions.report(new IllegalArgumentException("Ground altitude msl must be real: " + ground_altitude_msl));
        }
    }

}
