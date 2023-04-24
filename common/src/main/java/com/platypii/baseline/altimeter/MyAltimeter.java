package com.platypii.baseline.altimeter;

import com.platypii.baseline.location.LocationProvider;
import com.platypii.baseline.location.TimeOffset;
import com.platypii.baseline.measurements.MAltitude;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.measurements.MPressure;
import com.platypii.baseline.util.PubSub;
import com.platypii.baseline.util.PubSub.Subscriber;
import com.platypii.baseline.util.filters.Filter;
import com.platypii.baseline.util.filters.FilterKalman;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import androidx.annotation.NonNull;

/**
 * The main Altimeter class.
 * This class integrates sensor readings from barometer and GPS to model the altitude of the phone.
 * Altitude is measured both AGL and AMSL. Ground level is set to zero on initialization.
 */
public class MyAltimeter implements Subscriber<MPressure> {
    private static final String TAG = "MyAltimeter";

    @NonNull
    public final PubSub<MAltitude> altitudeEvents = new PubSub<>();

    @NonNull
    private final LocationProvider location;
    private boolean started = false;

    // Barometric altimeter
    @NonNull
    public final BaroAltimeter baro = new BaroAltimeter();
    public boolean barometerEnabled = true;

    // GPS altitude kalman filter
    @NonNull
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
    @NonNull
    public final GroundLevel groundLevel = new GroundLevel();

    // Sample counts
    public long baro_sample_count = 0;
    public long gps_sample_count = 0;

    private long lastFixMillis; // milliseconds

    // Capturing a method reference on instantiation since each time a method reference is used it creates a new synthetic lambda instance
    private final Subscriber<MLocation> updateGPSListener = this::updateGPS;

    public MyAltimeter(@NonNull LocationProvider location) {
        this.location = location;
    }

    /**
     * Initializes altimeter services, if not already running.
     * Starts async in a background thread
     *
     * @param context The Application context
     */
    public void start(@NonNull final Context context) {
        AsyncTask.execute(() -> {
            if (!started) {
                // Start barometer
                started = true;
                baro.start(context);
                baro.pressureEvents.subscribe(this);

                // Load ground level from preferences
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                groundLevel.start(prefs);

                // Start GPS updates
                location.locationUpdates.subscribe(updateGPSListener);
            } else {
                Log.e(TAG, "MyAltimeter already started");
            }
        });
    }

    public double altitudeAGL() {
        return groundLevel.altitudeAGL();
    }

    /**
     * Process new barometer reading
     */
    @Override
    public void apply(@NonNull MPressure pressure) {
        if (!barometerEnabled) return;

        lastFixMillis = TimeOffset.phoneToGpsTime(pressure.millis); // Convert to GPS time

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
        if (!Double.isNaN(loc.altitude_gps)) {
            if (barometerEnabled && baro_sample_count > 0) {
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
            if (lastLoc == null) {
                // Initial update
                gpsFilter.update(loc.altitude_gps, 0);
            } else {
                final long deltaTime = loc.millis - lastLoc.millis; // time since last gps altitude
                gpsFilter.update(loc.altitude_gps, deltaTime * 0.001);
            }
            lastLoc = loc;

            // Use gps for altitude instead of barometer
            if (!barometerEnabled || baro_sample_count == 0) {
                // No barometer use gps
                lastFixMillis = loc.millis;
                // Update the official altitude
                altitude = loc.altitude_gps;
                // Use kalman filter to compute climb rate
                // We don't use kalman for altitude, since gps probably already smoothing
                climb = gpsFilter.v();
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
        if (Double.isNaN(altitude)) {
            Log.e(TAG, "Altitude should not be NaN: altitude = " + altitude);
        }
        // Create the measurement
        final MAltitude myAltitude = new MAltitude(lastFixMillis, altitude, climb);
        altitudeEvents.post(myAltitude);
    }

    /**
     * GPS climb rate
     */
    public double gpsClimb() {
        return gpsFilter.v();
    }

    public void stop() {
        baro.pressureEvents.unsubscribe(this);
        baro.stop();
        location.locationUpdates.unsubscribe(updateGPSListener);
        if (started) {
            started = false;
        } else {
            Log.e(TAG, "MyAltimeter.stop() called, but service is already stopped");
        }
    }

}
