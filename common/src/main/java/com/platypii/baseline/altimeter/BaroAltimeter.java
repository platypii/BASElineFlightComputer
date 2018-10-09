package com.platypii.baseline.altimeter;

import com.platypii.baseline.BaseService;
import com.platypii.baseline.location.TimeOffset;
import com.platypii.baseline.measurements.MPressure;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.util.Numbers;
import com.platypii.baseline.util.RefreshRateEstimator;
import com.platypii.baseline.util.Stat;
import com.platypii.baseline.util.kalman.Filter;
import com.platypii.baseline.util.kalman.FilterKalman;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.util.Log;
import java.util.Arrays;
import org.greenrobot.eventbus.EventBus;

/**
 * Barometric altimeter with kalman filter.
 * Altitude is measured AGL. Ground level is set to zero on initialization.
 * Kalman filter is used to smooth barometer data.
 */
public class BaroAltimeter implements BaseService, SensorEventListener {
    private static final String TAG = "BaroAltimeter";

    private static final int sensorDelay = 100000; // microseconds
    private SensorManager sensorManager;

    private long lastFixNano; // nanoseconds

    // Pressure data
    public float pressure = Float.NaN; // hPa (millibars)
    public double pressure_altitude_raw = Double.NaN; // pressure converted to altitude under standard conditions (unfiltered)
    public double pressure_altitude_filtered = Double.NaN; // kalman filtered pressure altitude

    // Pressure altitude kalman filter
    private final Filter filter = new FilterKalman(); // Unfiltered(), AlphaBeta(), MovingAverage(), etc

    // Stats
    // Model error is the difference between our filtered output and the raw pressure altitude
    // Model error should approximate the sensor variance, even when in motion
    public final Stat model_error = new Stat();
    // Moving average of refresh rate in Hz
    public final RefreshRateEstimator refreshRate = new RefreshRateEstimator();

    /**
     * Initializes altimeter services, if not already running.
     * Starts async in a background thread
     * @param context The Application context
     */
    @Override
    public void start(@NonNull final Context context) {
        // Get a new preference manager
        if (sensorManager == null) {
            // Add sensor listener
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            if (sensorManager != null) {
                final Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
                if (sensor != null) {
                    // Start sensor updates
                    sensorManager.registerListener(BaroAltimeter.this, sensor, sensorDelay);
                }
            }
        } else {
            Log.e(TAG, "BaroAltimeter already started");
        }
    }

    /**
     * Process new barometer reading
     */
    @Override
    public void onSensorChanged(@NonNull SensorEvent event) {
        final long millis = System.currentTimeMillis(); // Record system time as soon as possible

        // Sanity checks
        // assert event.sensor.getType() == Sensor.TYPE_PRESSURE;
        if (event.values.length == 0 || Double.isNaN(event.values[0])) {
            Log.e(TAG, "Invalid update: " + Arrays.toString(event.values));
            return;
        }
        if (event.timestamp == lastFixNano) {
            Log.e(TAG, "Double update: " + lastFixNano);
            return;
        }
        if (event.timestamp < lastFixNano) {
            Log.e(TAG, "Negative time update: " + lastFixNano + " - " + event.timestamp + " = ");
            // Update lastFixNano and return
            // If we didn't update lastfix, altimeter would halt on one rogue future timestamp
            lastFixNano = event.timestamp;
            return;
        }

        // Convert system time to GPS time
        final long lastFixMillis = TimeOffset.phoneToGpsTime(millis);
        // Compute time since last sample in nanoseconds
        final long deltaTime = (lastFixNano == 0) ? 0 : (event.timestamp - lastFixNano);

        if (lastFixNano > 0 && deltaTime <= 0) {
            Log.e(TAG, "Non-monotonic time delta: " + event.timestamp + " - " + lastFixNano + " = " + deltaTime + " ns");
        }

        // Convert pressure to altitude
        pressure = event.values[0];
        pressure_altitude_raw = pressureToAltitude(pressure);

        // Barometer refresh rate
        refreshRate.addSample(event.timestamp / 1000000L);

        // Apply kalman filter to pressure altitude, to produce smooth barometric pressure altitude.
        if (lastFixNano <= 0) {
            filter.update(pressure_altitude_raw, 0);
        } else {
            filter.update(pressure_altitude_raw, deltaTime * 1E-9);
        }
        pressure_altitude_filtered = filter.x();
        final double climb = filter.v();

        // Altitude should never be null:
        if (!Numbers.isReal(pressure_altitude_filtered)) {
            Exceptions.report(new IllegalArgumentException("Invalid pressure altitude: " + pressure + " -> " + pressure_altitude_filtered));
            return;
        }

        // Compute model error
        model_error.addSample(pressure_altitude_filtered - pressure_altitude_raw);

        // Update last fix
        lastFixNano = event.timestamp;

        // Publish official altitude measurement
        final MPressure myPressure = new MPressure(lastFixMillis, lastFixNano, pressure_altitude_filtered, climb, pressure);
        EventBus.getDefault().post(myPressure);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    // Physical constants and ISA standard atmosphere
    private static final double pressure0 = SensorManager.PRESSURE_STANDARD_ATMOSPHERE; // ISA pressure 1013.25 hPa
//    private static final double temp0 = 288.15; // ISA temperature 15 degrees celcius
//    private static final double G = 9.80665; // Gravity (m/s^2)
//    private static final double R = 8.31432; // Universal Gas Constant ((N m)/(mol K))
//    private static final double M = 0.0289644; // Molar Mass of air (kg/mol)
//    private static final double L = -0.0065; // Temperature Lapse Rate (K/m)
    private static final double EXP = 0.190263237; // -L * R / (G * M);
    private static final double SCALE = 44330.76923; // -temp0 / L

    /**
     * Convert air pressure to altitude according to standard lapse rate.
     * alt = alt0 - (temp0 / L) * (1 - (pressure / pressure0)^(-LR/GM))
     *
     * @param pressure Pressure in hPa
     * @return The pressure altitude in meters
     */
    public static double pressureToAltitude(double pressure) {
        // Barometric formula
        return SCALE * (1 - Math.pow(pressure / pressure0, EXP));
    }

    @Override
    public void stop() {
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
            sensorManager = null;
        } else {
            Log.e(TAG, "BaroAltimeter.stop() called, but service is already stopped");
        }
    }

}
