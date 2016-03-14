package com.platypii.baseline.data;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import java.util.ArrayList;

import com.platypii.baseline.data.filter.Filter;
import com.platypii.baseline.data.filter.FilterKalman;
import com.platypii.baseline.data.measurements.MAltitude;
import com.platypii.baseline.data.measurements.MLocation;


/**
 * Altimeter manager
 * TODO: Get corrections via barometer, GPS, DEM
 * TODO: Model acceleration
 */
public class MyAltimeter {
    private static final String TAG = "MyAltimeter";

    private static SensorManager sensorManager;

    // Listeners
    private static final ArrayList<MyAltitudeListener> listeners = new ArrayList<>();

    // Pressure data
    public static float pressure = Float.NaN; // hPa (millibars)
    public static double pressure_altitude = Double.NaN; // pressure converted to altitude under standard conditions (unfiltered)
    public static double altitude_raw = Double.NaN; // pressure altitude adjusted for altitude offset (unfiltered)

    // GPS data
    public static double altitude_gps = Double.NaN;

    // official altitude = pressure_altitude - altitude_offset
    // altitude_offset uses GPS to get absolute altitude right
    public static double altitude_offset = 0.0;

    // Data filter
    private static final Filter filter = new FilterKalman(); // Unfiltered(), AlphaBeta(), MovingAverage(), etc

    // Official altitude data
    public static double altitude = Double.NaN; // Meters AMSL
    public static double climb = Double.NaN; // Rate of climb m/s
    // public static double verticalAcceleration = Double.NaN;

    public static long firstFixNano = -1; // nanoseconds
    private static long lastFixNano; // nanoseconds
    private static long lastFixMillis; // milliseconds uptime

    // History
    public static final SyncedList<MAltitude> history = new SyncedList<>();

    // Stats
    public static final Stat pressure_altitude_stat = new Stat(); // Statistics on the mean and variance of the sensor
    private static long n = 0; // number of samples
    public static float refreshRate = 0; // Moving average of refresh rate in Hz

    /**
     * Initializes altimeter services, if not already running
     * @param appContext The Application context
     */
    public static synchronized void start(@NonNull Context appContext) {
        if(sensorManager == null) {
            // Add sensor listener
            sensorManager = (SensorManager) appContext.getSystemService(Context.SENSOR_SERVICE);
            final Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            if (sensor != null) {
                // Start sensor updates
                sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
            }

            // Start GPS updates
            MyLocationManager.addListener(locationListener);
        } else {
            Log.w(TAG, "MyAltimeter already started");
        }
    }

    // Sensor Event Listener
    private static final SensorEventListener sensorEventListener = new AltimeterSensorEventListener();
    private static class AltimeterSensorEventListener implements SensorEventListener {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        public void onSensorChanged(@NonNull SensorEvent event) {
            long millis = System.currentTimeMillis(); // Record time as soon as possible
            assert event.sensor.getType() == Sensor.TYPE_PRESSURE;
            // Log.w(TAG, "values[] = " + event.values[0] + ", " + event.values[1] + ", " + event.values[2]);
            MyAltimeter.updateBarometer(millis, event);
        }
    }

    // Location Listener
    private static final MyLocationListener locationListener = new AltimeterLocationListener();
    private static class AltimeterLocationListener implements MyLocationListener {
        public void onLocationChanged(@NonNull MLocation loc) {
            MyAltimeter.updateGPS(loc);
        }
    }

    /**
     * Process new barometer reading
     */
    private static void updateBarometer(long millis, SensorEvent event) {
        double prevAltitude = altitude;
        // double prevClimb = climb;
        long prevLastFixNano = lastFixNano;

        assert event != null;
        assert event.accuracy == 0;
        pressure = event.values[0];
        if(firstFixNano == -1) {
            firstFixNano = event.timestamp;
        }
        lastFixNano = event.timestamp;
        lastFixMillis = millis;

        // Barometer refresh rate
        final long deltaTime = lastFixNano - prevLastFixNano; // time since last refresh
        if(deltaTime > 0) {
            final float refreshTime = 1E9f / (float) (deltaTime);
            refreshRate += (refreshTime - refreshRate) * 0.5f; // Moving average
            if (Double.isNaN(refreshRate)) {
                Log.e(TAG, "Refresh rate is NaN, deltaTime = " + deltaTime + " refreshTime = " + refreshTime);
                refreshRate = 0;
            }
        }

        // Convert pressure to altitude
        pressure_altitude = pressureToAltitude(pressure);

        altitude_raw = pressure_altitude - altitude_offset; // the current pressure converted to altitude AMSL. noisy.
        
        // Update the official altitude
        final double dt = Double.isNaN(prevAltitude)? 0 : (lastFixNano - prevLastFixNano) * 1E-9;
        // Log.d(TAG, "Raw Altitude AGL: " + Convert.distance(altitude_raw) + ", dt = " + dt);

        filter.update(pressure_altitude, dt);

        altitude = filter.x - altitude_offset;
        climb = filter.v;

        // Log.d("Altimeter", "alt = " + altitude + ", climb = " + climb);

        if(Double.isNaN(altitude)) {
            Log.w(TAG, "Altitude should not be NaN: altitude = " + altitude);
        }

        n++;
        updateAltitude();
    }

    private static long gps_sample_count = 0;
    /**
     * Process new GPS reading
     */
    private static void updateGPS(MLocation loc) {
        if(!Double.isNaN(loc.altitude_gps)) {
            altitude_gps = loc.altitude_gps;

            if(n > 0) {
                // Log.d(TAG, "alt = " + altitude + ", alt_gps = " + altitude_gps + ", offset = " + altitude_offset);
                if(gps_sample_count == 0) {
                    // First altitude reading. Calibrate ground level.
                    altitude_offset = pressure_altitude - altitude_gps;
                } else if(gps_sample_count < 10) {
                    // Average the first N samples
                    altitude_offset += (altitude_raw - altitude_gps) / (n + 1);
                } else {
                    // Use GPS to correct barometer drift (moving average)
                    altitude_offset += (altitude_raw - altitude_gps) / 10;
                }
            } else {
                // No barometer use gps
                final double prevAltitude = altitude;
                final long prevLastFix = lastFixMillis;
                lastFixMillis = loc.millis;
                // Update the official altitude
                altitude = altitude_gps;
                if(Double.isNaN(prevAltitude)) {
                    climb = 0;
                } else {
                    final double dt = (lastFixMillis - prevLastFix) * 1E-3;
                    climb = (altitude - prevAltitude) / dt; // m/s
                }
            }
            gps_sample_count++;
            updateAltitude();
        }
    }

    /**
     * Saves an official altitude measurement
     */
    private static void updateAltitude() {
        // Create the measurement
        final MAltitude myAltitude = new MAltitude(lastFixNano, altitude, climb, pressure);
        history.append(myAltitude);
        // Notify listeners (using AsyncTask so the altimeter never blocks!)
        pressure_altitude_stat.addSample(pressure_altitude);
        new AsyncTask<MAltitude,Void,Void>() {
            @Override
            protected Void doInBackground(MAltitude... params) {
                synchronized(listeners) {
                    for(MyAltitudeListener listener : listeners) {
                        listener.altitudeDoInBackground(params[0]);
                    }
                }
                return null;
            }
            @Override
            protected void onPostExecute(Void result) {
                for(MyAltitudeListener listener : listeners) {
                    listener.altitudeOnPostExecute();
                }
            }
        }.execute(myAltitude);
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
    private static final double EXP = 0.190263237;// - L * R / (G * M);

    /**
     * Convert air pressure to altitude
     * @param pressure Pressure in hPa
     * @return The pressure altitude in meters
     */
    private static double pressureToAltitude(double pressure) {
        // Barometric formula
        return altitude0 - temp0 * (1 - Math.pow(pressure / pressure0, EXP)) / L;
    }

    /**
     * Add a new listener for us to notify
     */
    public static void addListener(MyAltitudeListener listener) {
        synchronized(listeners) {
            listeners.add(listener);
        }
    }
    public static void removeListener(MyAltitudeListener listener) {
        synchronized(listeners) {
            listeners.remove(listener);
        }
    }

    public static void stop() {
        MyLocationManager.removeListener(locationListener);
        sensorManager.unregisterListener(sensorEventListener);
        sensorManager = null;

        if(listeners.size() > 0) {
            Log.e(TAG, "Stopping location service, but listeners are still listening");
        }
    }

}
