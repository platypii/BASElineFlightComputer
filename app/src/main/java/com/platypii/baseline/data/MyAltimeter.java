package com.platypii.baseline.data;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.util.Log;
import java.util.ArrayList;

import com.platypii.baseline.data.filter.Filter;
import com.platypii.baseline.data.filter.FilterKalman;


// Altimeter manager
// Super important so it gets it's own class
// TODO: Get corrections via barometer, GPS, DEM
// TODO: Show visual warning when no altimeter data
// TODO: Acceleration
public class MyAltimeter {
    
    // Singleton MyAltimeter
    private static MyAltimeter _instance;
    
    // Listeners
    private static final ArrayList<MyAltitudeListener> listeners = new ArrayList<>();

    // Pressure data
    public static float pressure = Float.NaN; // hPa (millibars)
    public static double pressure_altitude = Double.NaN; // pressure converted to altitude under standard conditions (unfiltered)
    public static double altitude_raw = Double.NaN; // pressure altitude adjusted for ground level (unfiltered)
    
    // GPS data
    private static double altitude_gps = Double.NaN;
    
    // Ground level
    public static double ground_level = Double.NaN;
    
    // Data filter
    private static final Filter filter = new FilterKalman(); // Unfiltered(), AlphaBeta(), MovingAverage(), etc
    
    // Official altitude data
    public static double altitude = Double.NaN; // Meters AGL
    public static double climb = Double.NaN; // Rate of climb m/s
    // public static double verticalAcceleration = Double.NaN;
    
    private static long lastFixNano; // nanoseconds
    private static long lastFixMillis; // milliseconds uptime

    // History
	private static final int maxHistory = 5 * 60; // Maximum number of measurements to keep in memory
	public static final SyncedList<MyAltitude> history = new SyncedList<>(maxHistory);
    // public static MyAltitude myAltitude; // Measurement
    public static final Stat pressure_altitude_stat = new Stat(); // Statistics on the mean and variance of the sensor
    private static int n = 0; // number of samples

    
    /**
     * Initializes altimeter services, if not already running
     * @param context The Application context
     */
    public static synchronized void initAltimeter(Context context) {
        if(_instance == null) {
            _instance = new MyAltimeter();
            
            // Add sensor listener
            SensorManager sensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
            Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
            if(sensor != null) {
                // Start sensor updates
                sensorManager.registerListener(sensorEventListener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
            }
            
            // Start GPS updates
            MyLocationManager.addListener(locationListener);
        }
    }

    // Sensor Event Listener
    private static final SensorEventListener sensorEventListener = new AltimeterSensorEventListener();
    private static class AltimeterSensorEventListener implements SensorEventListener {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        public void onSensorChanged(SensorEvent event) {
            long millis = System.currentTimeMillis(); // Record time as soon as possible
            assert event.sensor.getType() == Sensor.TYPE_PRESSURE;
            // Log.w("Altimeter", "values[] = " + event.values[0] + ", " + event.values[1] + ", " + event.values[2]);
            MyAltimeter.updateBarometer(millis, event);
        }
    }

    // Location Listener
    private static final MyLocationListener locationListener = new AltimeterLocationListener();
    private static class AltimeterLocationListener implements MyLocationListener {
        public void onLocationChanged(MyLocation loc) {
            MyAltimeter.updateGPS(loc);
        }
    }

    /**
     * Process new barometer reading
     */
    private static void updateBarometer(long millis, SensorEvent event) {
        double prevAltitude = altitude;
        // double prevClimb = climb;
        long prevLastFix = lastFixNano;
        
        assert event != null;
        assert event.accuracy == 0;
        pressure = event.values[0];
        lastFixNano = event.timestamp;
        lastFixMillis = millis;

        // Convert pressure to altitude
        pressure_altitude = pressureToAltitude(pressure);
        
        // Adjust for ground level
        if(n == 0) {
            // First pressure reading. Calibrate ground level.
            ground_level = pressure_altitude;
        } else if(n < 16) {
            // Average the first N samples
            ground_level += (pressure_altitude - ground_level) / (n + 1);
        }
        n++;
        altitude_raw = pressure_altitude - ground_level; // the current pressure converted to altitude AGL. noisy.
        
        // Update the official altitude
        double dt = Double.isNaN(prevAltitude)? 0 : (lastFixNano - prevLastFix) * 1E-9;
        // Log.d("Altimeter", "Raw Altitude AGL: " + Convert.distance(altitude_raw) + ", dt = " + dt);

        filter.update(pressure_altitude, dt);

        altitude = filter.x - ground_level;
		climb = filter.v;

        // Log.d("Altimeter", "alt = " + altitude + ", climb = " + climb);

        updateAltitude();
        
    }

    /**
     * Process new GPS reading
     */
    private static void updateGPS(MyLocation loc) {
    	// TODO: Use GPS to correct barometer drift (Kalman)

    	// If barometer is not present, fall back to GPS
    	if(Double.isNaN(pressure_altitude) && !Double.isNaN(loc.altitude_gps)) {
            double prevAltitude = altitude;
            long prevLastFix = lastFixMillis;

            altitude_gps = loc.altitude_gps;
            lastFixMillis = loc.timeMillis;
            
            // Adjust for ground level
            if(n == 0) {
                // First altitude reading. Calibrate ground level.
                ground_level = altitude_gps;
            } else if(n < 20) {
                // Average the first N samples
                ground_level += (altitude_gps - ground_level) / (n + 1);
            }
            n++;
    		
            // Update the official altitude
            if(Double.isNaN(prevAltitude)) {
                altitude = altitude_gps - ground_level;
                climb = 0;
            } else {
                double dt = (lastFixMillis - prevLastFix) * 1E-3;
                altitude = altitude_gps - ground_level;
                climb = (altitude - prevAltitude) / dt; // m/s
            }

            updateAltitude();
    	}
    }
    
    /**
     * Saves an official altitude measurement
     */
    private static void updateAltitude() {
    	// Create the measurement
    	MyAltitude myAltitude = new MyAltitude(lastFixMillis, altitude, climb, pressure, altitude_gps);
    	history.addLast(myAltitude);
        // Notify listeners (using AsyncTask so the altimeter never blocks!)
        pressure_altitude_stat.addSample(pressure_altitude);
        new AsyncTask<MyAltitude,Void,Void>() {
            @Override
            protected Void doInBackground(MyAltitude... params) {
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

    /**
     * Set the ground level offset
     */
    public static void setGroundLevel(double new_ground_level) {
        altitude += ground_level - new_ground_level;
        ground_level = new_ground_level;
        Log.i("Altimeter", "Setting ground level = " + ground_level);
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

}
