package com.platypii.baseline.altimeter;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.platypii.baseline.Service;
import com.platypii.baseline.Services;
import com.platypii.baseline.measurements.MPressure;
import com.platypii.baseline.util.Stat;

import org.greenrobot.eventbus.EventBus;

/**
 * Barometric altimeter with kalman filter.
 * Altitude is measured AGL. Ground level is set to zero on initialization.
 * Kalman filter is used to smooth barometer data.
 */
public class BaroAltimeter implements Service, SensorEventListener {
    private static final String TAG = "BaroAltimeter";

    private static final int sensorDelay = 100000; // microseconds
    private SensorManager sensorManager;

    // Pressure data
    public float pressure = Float.NaN; // hPa (millibars)
    public double pressure_altitude_raw = Double.NaN; // pressure converted to altitude under standard conditions (unfiltered)
    public double pressure_altitude_filtered = Double.NaN; // kalman filtered pressure altitude

    // Pressure altitude kalman filter
    private final Filter filter = new FilterKalman(); // Unfiltered(), AlphaBeta(), MovingAverage(), etc

    // Official altitude data
    public double climb = Double.NaN; // Rate of climb m/s
    // public static double verticalAcceleration = Double.NaN;

    private long lastFixNano; // nanoseconds
    private long lastFixMillis; // milliseconds

    // Stats
    // Model error is the difference between our filtered output and the raw pressure altitude
    // Model error should approximate the sensor variance, even when in motion
    public final Stat model_error = new Stat();
    public float refreshRate = 0; // Moving average of refresh rate in Hz
    public long sample_count = 0; // number of samples

    /**
     * Initializes altimeter services, if not already running.
     * Starts async in a background thread
     * @param context The Application context
     */
    @Override
    public synchronized void start(@NonNull final Context context) {
        // Get a new preference manager
        if(sensorManager == null) {
            // Add sensor listener
            sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            final Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            if (sensor != null) {
                // Start sensor updates
                sensorManager.registerListener(BaroAltimeter.this, sensor, sensorDelay);
            }
        } else {
            Log.e(TAG, "BaroAltimeter already started");
        }
    }

    /** SensorEventListener */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    @Override
    public void onSensorChanged(@NonNull SensorEvent event) {
        long millis = System.currentTimeMillis(); // Record time as soon as possible
        // assert event.sensor.getType() == Sensor.TYPE_PRESSURE;
        // Log.w(TAG, "values[] = " + event.values[0] + ", " + event.values[1] + ", " + event.values[2]);
        updateBarometer(millis, event);
    }

    /**
     * Process new barometer reading
     */
    private void updateBarometer(long millis, SensorEvent event) {
        if(event == null || event.values.length == 0 || Double.isNaN(event.values[0]))
            return;

        if(lastFixNano == event.timestamp) {
            Log.e(TAG, "Double update: " + lastFixNano);
        }
        final long prevLastFixNano = lastFixNano;

        pressure = event.values[0];
        lastFixNano = event.timestamp;
        lastFixMillis = millis - Services.location.phoneOffsetMillis; // Convert to GPS time

        // Barometer refresh rate
        final long deltaTime = lastFixNano - prevLastFixNano; // time since last refresh
        if(deltaTime > 0 && prevLastFixNano > 0) {
            final float newRefreshRate = 1E9f / (float) (deltaTime); // Refresh rate based on last 2 samples
            if(refreshRate == 0) {
                refreshRate = newRefreshRate;
            } else {
                refreshRate += (newRefreshRate - refreshRate) * 0.5f; // Moving average
            }
            if (Double.isNaN(refreshRate)) {
                Log.e(TAG, "Refresh rate is NaN, deltaTime = " + deltaTime + " refreshTime = " + newRefreshRate);
                FirebaseCrash.report(new Exception("Refresh rate is NaN, deltaTime = " + deltaTime + " newRefreshRate = " + newRefreshRate));
                refreshRate = 0;
            }
        }

        // Convert pressure to altitude
        pressure_altitude_raw = pressureToAltitude(pressure);

        // Apply kalman filter to pressure altitude, to produce smooth barometric pressure altitude.
        final double dt = (prevLastFixNano == 0)? 0 : (lastFixNano - prevLastFixNano) * 1E-9;
        filter.update(pressure_altitude_raw, dt);
        pressure_altitude_filtered = filter.x;
        climb = filter.v;

        // Compute model error
        model_error.addSample(pressure_altitude_filtered - pressure_altitude_raw);

        sample_count++;
        updateAltitude();
    }

    /**
     * Saves an official altitude measurement
     */
    private void updateAltitude() {
        // Log.d(TAG, "Altimeter Update Time: " + System.currentTimeMillis() + " " + System.nanoTime() + " " + lastFixMillis + " " + lastFixNano);
        // Create the measurement
        final MPressure myPressure = new MPressure(lastFixMillis, lastFixNano, pressure_altitude_filtered, climb, pressure);
        EventBus.getDefault().post(myPressure);
    }

    // ISA pressure and temperature
    private static final double altitude0 = 0; // ISA height 0 meters
    private static final double pressure0 = SensorManager.PRESSURE_STANDARD_ATMOSPHERE; // ISA pressure 1013.25 hPa
    private static final double temp0 = 288.15; // ISA temperature 15 degrees celcius

    // Physical constants
//    private static final double G = 9.80665; // Gravity (m/s^2)
//    private static final double R = 8.31432; // Universal Gas Constant ((N m)/(mol K))
//    private static final double M = 0.0289644; // Molar Mass of air (kg/mol)
    private static final double L = -0.0065; // Temperature Lapse Rate (K/m)
    private static final double EXP = 0.190263237; // -L * R / (G * M);

    /**
     * Convert air pressure to altitude
     * @param pressure Pressure in hPa
     * @return The pressure altitude in meters
     */
    private static double pressureToAltitude(double pressure) {
        // Barometric formula
        return altitude0 - temp0 * (1 - Math.pow(pressure / pressure0, EXP)) / L;
    }

    @Override
    public void stop() {
        if(sensorManager != null) {
            sensorManager.unregisterListener(this);
            sensorManager = null;
        } else {
            Log.e(TAG, "BaroAltimeter.stop() called, but service is already stopped");
        }
    }

}
