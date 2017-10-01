package com.platypii.baseline.altimeter;

import com.platypii.baseline.Service;
import com.platypii.baseline.location.LocationProvider;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.location.TimeOffset;
import com.platypii.baseline.measurements.MAltitude;
import com.platypii.baseline.measurements.MLocation;
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
 *
 * TODO: Correct barometer drift with GPS
 */
public class MyAltimeter implements Service, MyLocationListener {
    private static final String TAG = "MyAltimeter";

    private final LocationProvider location;
    private boolean started = false;

    // Barometric altimeter
    public final BaroAltimeter baro = new BaroAltimeter();

    // GPS altitude kalman filter
    private final Filter gpsFilter = new FilterKalman();
    private MLocation lastLoc;

    // official altitude AMSL = pressure_altitude - altitude_offset
    // altitude_offset uses GPS to get absolute altitude right
    private double altitude_offset = 0;

    // Official altitude data
    public double altitude = Double.NaN; // Meters AMSL
    public double climb = Double.NaN; // Rate of climb m/s
    // public static double verticalAcceleration = Double.NaN;

    // Ground level
    final GroundLevel groundLevel = new GroundLevel();

    // Sample counts
    public long baro_sample_count = 0;
    public long gps_sample_count = 0;

    private long lastFixMillis; // milliseconds

    public MyAltimeter(LocationProvider location) {
        this.location = location;
    }

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
                if(!started) {
                    // Start barometer
                    started = true;
                    EventBus.getDefault().register(MyAltimeter.this);
                    baro.start(context);

                    // Load ground level from preferences
                    final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    groundLevel.start(prefs);

                    // Start GPS updates
                    if(location != null) {
                        location.addListener(MyAltimeter.this);
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
        return groundLevel.altitudeAGL();
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
        lastFixMillis = pressure.millis - TimeOffset.phoneOffsetMillis; // Convert to GPS time

        // Compute GPS corrected altitude AMSL
        altitude = baro.pressure_altitude_filtered - altitude_offset;
        climb = pressure.climb;

        // Update ground level
        groundLevel.onPressureEvent(pressure);
        baro_sample_count++;

        updateAltitude();
    }

    /**
     * Process new GPS reading
     */
    private void updateGPS(@NonNull MLocation loc) {
        // Log.d(TAG, "GPS Update Time: " + System.currentTimeMillis() + " " + System.nanoTime() + " " + loc.millis);
        if(!Double.isNaN(loc.altitude_gps)) {
            if(baro_sample_count > 0) {
                // GPS correction for altitude AMSL
                if (gps_sample_count == 0) {
                    // First altitude reading. Calibrate ground level.
                    altitude_offset = baro.pressure_altitude_filtered - loc.altitude_gps;
                } else {
                    // Average the first N samples, then use moving average with lag 20
                    final double altitude_error = altitude - loc.altitude_gps;
                    final long correction_factor = Math.min(gps_sample_count, 20);
                    final double altitude_correction = altitude_error / correction_factor;
                    altitude_offset += altitude_correction;
                }
            }

            // Update gps kalman filter
            if(lastLoc != null) {
                final long deltaTime = loc.millis - lastLoc.millis; // time since last gps altitude
                gpsFilter.update(loc.altitude_gps, deltaTime * 0.001);
            } else {
                gpsFilter.update(loc.altitude_gps, 0);
            }
            lastLoc = loc;

            // Use gps for altitude instead of barometer
            if(baro_sample_count == 0) {
                // No barometer use gps
                lastFixMillis = loc.millis;
                // Update the official altitude
                altitude = loc.altitude_gps;
                // Use kalman filter to compute climb rate
                // We don't use kalman for altitude, since gps probably already smoothing
                climb = gpsFilter.v;
                // Only update official altitude if we are relying solely on GPS for altitude
                updateAltitude();
            }

            // Update ground level
            groundLevel.onLocationEvent(loc);
            gps_sample_count++;
        }
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
        EventBus.getDefault().post(myAltitude);
    }

    /**
     * GPS climb rate
     */
    public double gpsClimb() {
        return gpsFilter.v;
    }

    @Override
    public void stop() {
        location.removeListener(this);
        EventBus.getDefault().unregister(this);
        if (started) {
            started = false;
        } else {
            Log.e(TAG, "MyAltimeter.stop() called, but service is already stopped");
        }
    }

}
