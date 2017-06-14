package com.platypii.baseline.alti;

import com.platypii.baseline.measurements.MAltitude;
import com.platypii.baseline.measurements.MPressure;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * The main Altimeter class.
 * This class integrates sensor readings from barometer and GPS to model the altitude of the phone.
 * Altitude is measured both AGL and AMSL. Ground level is set to zero on initialization.
 * Kalman filter is used to smooth barometer data.
 *
 * TODO: Correct barometer drift with GPS
 */
public class MyAltimeter {
    private static final String TAG = "MyAltimeter";

    private SharedPreferences prefs;

    // Barometric altimeter
    private BaroAltimeter baro = new BaroAltimeter();

    // Official altitude data
    private double altitude = Double.NaN; // Meters AMSL
    private double climb = Double.NaN; // Rate of climb m/s
    // public double verticalAcceleration = Double.NaN;

    // Ground level
    // Save ground level for 12 hours (in milliseconds)
    private static final long GROUND_LEVEL_TTL = 12 * 60 * 60 * 1000;
    private boolean ground_level_initialized = false;
    double ground_level = Double.NaN;

    // Sample counts
    private long baro_sample_count = 0;

    private long lastFixMillis; // milliseconds

    /**
     * Initializes altimeter services, if not already running.
     * Starts async in a background thread
     * @param context The Application context
     */
    public synchronized void start(@NonNull final Context context) {
        Log.i(TAG, "Starting altimeter");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                if(prefs == null) {
                    // Get a new preference manager
                    prefs = PreferenceManager.getDefaultSharedPreferences(context);

                    // Start barometer
                    EventBus.getDefault().register(MyAltimeter.this);
                    baro.start(context);

                    // Load ground level from preferences
                    loadGroundLevel();
                } else {
                    Log.e(TAG, "MyAltimeter already started");
                }
            }
        });
    }

    double altitudeAGL() {
        return baro.pressure_altitude_filtered - ground_level;
    }

    /**
     * Load ground level from preferences
     */
    private void loadGroundLevel() {
        final long groundLevelTime = prefs.getLong("altimeter_ground_level_time", -1L);
        if(groundLevelTime != -1 && System.currentTimeMillis() - groundLevelTime < GROUND_LEVEL_TTL) {
            ground_level = prefs.getFloat("altimeter_ground_level", 0);
            ground_level_initialized = true;
            Log.i(TAG, "Restoring ground level from preferences: " + Convert.distance(ground_level, 2, true));
        } else {
            Log.i(TAG, "Initializing ground level to zero");
        }
    }

    /**
     * Set ground level, based on pressure altitude.
     * Should not be called until altimeter is initialized.
     * @param groundLevel the pressure altitude at ground level (0m AGL)
     */
    void setGroundLevel(double groundLevel) {
        ground_level = groundLevel;
        ground_level_initialized = true;
        // Save to preferences
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putFloat("altimeter_ground_level", (float) ground_level);
        edit.putLong("altimeter_ground_level_time", System.currentTimeMillis());
        edit.apply();
    }

    /**
     * Process new barometer reading
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPressureEvent(MPressure pressure) {
        lastFixMillis = pressure.millis;
//        lastFixMillis = pressure.millis - Services.location.phoneOffsetMillis; // TODO: Convert to GPS time

        // TODO: Compute GPS corrected altitude AMSL
//        altitude = baro.pressure_altitude_filtered - altitude_offset;
        altitude = baro.pressure_altitude_filtered;
        climb = pressure.climb;

        // Adjust for ground level
        if (!ground_level_initialized) {
            if (baro_sample_count == 0) {
                // First pressure reading. Calibrate ground level.
                ground_level = baro.pressure_altitude_raw;
            } else if (baro_sample_count < 30) {
                // Average the first N raw samples
                // Note: because we divide by baro_sample_count, we actually discard the first sample.
                // This is intentional, because Moto360 gives a garbage first reading.
                ground_level += (baro.pressure_altitude_raw - ground_level) / baro_sample_count;
            } else {
                setGroundLevel(ground_level);
            }
        }
        baro_sample_count++;

        updateAltitude();
    }


    /**
     * Saves an official altitude measurement
     */
    private void updateAltitude() {
        // Log.d(TAG, "Altimeter Update Time: " + System.currentTimeMillis() + " " + System.nanoTime() + " " + lastFixMillis + " " + lastFixNano);
        if(Double.isNaN(altitude)) {
            Log.e(TAG, "Altitude should not be NaN: altitude = " + altitude);
        }
        // Create the measurement
        final MAltitude myAltitude = new MAltitude(lastFixMillis, altitude, climb);
        // Notify listeners
        EventBus.getDefault().post(myAltitude);
    }

    public void stop() {
        EventBus.getDefault().unregister(this);
        if(prefs != null) {
            prefs = null;
        } else {
            Log.e(TAG, "MyAltimeter.stop() called, but service is already stopped");
        }
    }

}
