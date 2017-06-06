package com.platypii.baseline.altimeter;

import com.platypii.baseline.Service;
import com.platypii.baseline.Services;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.measurements.MAltitude;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.measurements.MPressure;
import com.platypii.baseline.util.Convert;
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
public class MyAltimeter implements Service, MyLocationListener {
    private static final String TAG = "MyAltimeter";

    private SharedPreferences prefs;

    // Barometric altimeter
    public BaroAltimeter baro = new BaroAltimeter();

    // official altitude AMSL = pressure_altitude - altitude_offset
    // altitude_offset uses GPS to get absolute altitude right
    private double altitude_offset = 0.0;

    // Official altitude data
    public double altitude = Double.NaN; // Meters AMSL
    public double climb = Double.NaN; // Rate of climb m/s
    // public static double verticalAcceleration = Double.NaN;

    // Ground level
    // Save ground level for 12 hours (in milliseconds)
    private static final long GROUND_LEVEL_TTL = 12 * 60 * 60 * 1000;
    private boolean ground_level_initialized = false;
    private double ground_level = Double.NaN;

    private long lastFixMillis; // milliseconds

    /**
     * Initializes altimeter services, if not already running.
     * Starts async in a background thread
     * @param context The Application context
     */
    @Override
    public synchronized void start(@NonNull final Context context) {
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

                    // Start GPS updates
                    if(Services.location != null) {
                        Services.location.addListener(MyAltimeter.this);
                    } else {
                        Log.e(TAG, "Location services should be initialized before altimeter");
                    }
                } else {
                    Log.e(TAG, "MyAltimeter already started");
                }
            }
        });
    }

    public double altitudeAGL() {
        return baro.pressure_altitude_filtered - ground_level;
    }

    public double groundLevel() {
        return ground_level;
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

    /** Location Listener */
    @Override
    public void onLocationChanged(@NonNull MLocation loc) {
        updateGPS(loc);
    }
    @Override
    public void onLocationChangedPostExecute() {}

    /**
     * Process new barometer reading
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onPressureEvent(MPressure pressure) {
        lastFixMillis = pressure.millis - Services.location.phoneOffsetMillis; // Convert to GPS time

        // Compute GPS corrected altitude AMSL
        altitude = baro.pressure_altitude_filtered - altitude_offset;
        if(Double.isNaN(altitude)) {
            Log.w(TAG, "Altitude should not be NaN: altitude = " + altitude);
        }
        climb = pressure.climb;

        // Adjust for ground level
        if(!ground_level_initialized) {
            if (baro.sample_count == 0) {
                // First pressure reading. Calibrate ground level.
                ground_level = baro.pressure_altitude_raw;
            } else if (baro.sample_count < 30) {
                // Average the first N raw samples
                ground_level += (baro.pressure_altitude_raw - ground_level) / (baro.sample_count + 1);
            } else {
                setGroundLevel(ground_level);
            }
        }

        updateAltitude();
    }

    private long gps_sample_count = 0;
    /**
     * Process new GPS reading
     */
    private void updateGPS(MLocation loc) {
        // Log.d(TAG, "GPS Update Time: " + System.currentTimeMillis() + " " + System.nanoTime() + " " + loc.millis);
        if(!Double.isNaN(loc.altitude_gps)) {
            if(baro.sample_count > 0) {
                // Log.d(TAG, "alt = " + altitude + ", alt_gps = " + altitude_gps + ", offset = " + altitude_offset);
                // GPS correction for altitude AMSL
                if(gps_sample_count == 0) {
                    // First altitude reading. Calibrate ground level.
                    altitude_offset = baro.pressure_altitude_filtered - loc.altitude_gps;
                } else {
                    // Average the first N samples, then use moving average with lag 20
                    final double altitude_error = altitude - loc.altitude_gps;
                    final long correction_factor = Math.min(gps_sample_count, 20);
                    final double altitude_correction = altitude_error / correction_factor;
                    altitude_offset += altitude_correction;
                }
            } else {
                // No barometer use gps
                final double prevAltitude = altitude;
                final long prevLastFix = lastFixMillis;
                lastFixMillis = loc.millis;
                // Update the official altitude
                altitude = loc.altitude_gps;
                // TODO: Use kalman filter to compute climb rate
                if(Double.isNaN(prevAltitude)) {
                    climb = 0;
                } else {
                    final double dt = (lastFixMillis - prevLastFix) * 1E-3;
                    climb = (altitude - prevAltitude) / dt; // m/s
                }
                // Only update official altitude if we are relying solely on GPS for altitude
                updateAltitude();
            }
            gps_sample_count++;
        }
    }

    /**
     * Saves an official altitude measurement
     */
    private void updateAltitude() {
        // Log.d(TAG, "Altimeter Update Time: " + System.currentTimeMillis() + " " + System.nanoTime() + " " + lastFixMillis + " " + lastFixNano);
        // Create the measurement
        final MAltitude myAltitude = new MAltitude(lastFixMillis, altitude, climb);
        EventBus.getDefault().post(myAltitude);
    }

    @Override
    public void stop() {
        Services.location.removeListener(this);
        EventBus.getDefault().unregister(this);
        if(prefs != null) {
            prefs = null;
        } else {
            Log.e(TAG, "MyAltimeter.stop() called, but service is already stopped");
        }
    }

}
