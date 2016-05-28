package com.platypii.baseline.data;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.platypii.baseline.data.measurements.MAccel;
import com.platypii.baseline.data.measurements.MGravity;
import com.platypii.baseline.data.measurements.MRotation;
import com.platypii.baseline.data.measurements.MSensor;
import com.platypii.baseline.util.SyncedList;

public class MySensorManager {

    // Singleton MySensorManager
    private static MySensorManager _instance;

    // Android sensor manager (accelerometer, gravity, gyro, linear accel, magnetic, pressure, humidity, rotation, temp)
    private static SensorManager sensorManager;

    // History
    public static final SyncedList<MSensor> accel = new SyncedList<>();
    public static final SyncedList<MSensor> gravity = new SyncedList<>();
    public static final SyncedList<MSensor> rotation = new SyncedList<>();

    private static final List<MySensorListener> listeners = new ArrayList<>();

    private static final SensorEventListener androidSensorListener = new SensorEventListener() {
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
        public void onSensorChanged(@NonNull SensorEvent event) {
            final long t = event.timestamp; // nano
            final float x = event.values[0];
            final float y = event.values[1];
            final float z = event.values[2];
            MSensor measurement = null;
            // Update sensor histories
            switch(event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    measurement = new MAccel(t, (float)Math.sqrt(x*x + y*y + z*z));
                    accel.append(measurement);
                    break;
                case Sensor.TYPE_GRAVITY:
                    measurement = new MRotation(t,x,y,z);
                    gravity.append(measurement);
                    break;
                case Sensor.TYPE_ROTATION_VECTOR:
                case Sensor.TYPE_MAGNETIC_FIELD:
                    measurement = new MGravity(t,x,y,z);
                    rotation.append(measurement);
                    break;
                default:
                    Log.e("MySensorManager", "Received unexpected sensor event");
            }
            // Notify listeners
            if(measurement != null) {
                for(MySensorListener listener : listeners) {
                    listener.onSensorChanged(measurement);
                }
            }
        }
    };

    /**
     * Initializes location services
     *
     * @param appContext The Application context
     */
    public static synchronized void start(@NonNull Context appContext) {
        if(_instance == null) {
            _instance = new MySensorManager();

            sensorManager = (SensorManager)appContext.getSystemService(Context.SENSOR_SERVICE);

            // Sensors
            final Sensor accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            final Sensor gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
            final Sensor rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

            sensorManager.registerListener(androidSensorListener, accelSensor, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(androidSensorListener, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
            sensorManager.registerListener(androidSensorListener, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST);

//          startSensor(Sensor.TYPE_MAGNETIC_FIELD);
//          startSensor(Sensor.TYPE_LINEAR_ACCELERATION);
//          startSensor(Sensor.TYPE_GYROSCOPE);
//          startSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
//          startSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        }
    }

    /**
     * Returns a string representation of all available sensors
     */
//    public static CharSequence getSensorsString() {
//        StringBuffer buffer = new StringBuffer();
//        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
//        for(Sensor sensor : sensors) {
//            buffer.append(sensor.getVendor());
//            buffer.append(" - ");
//            buffer.append(sensor.getName());
//            buffer.append('\n');
//        }
//        return buffer;
//    }

    /**
     * Add a new listener to be notified of location updates
     */
    public static void addListener(MySensorListener listener) {
        synchronized(listeners) {
            listeners.add(listener);
        }
    }

    /**
     * Remove a listener from location updates
     */
    public static void removeListener(MySensorListener listener) {
        synchronized(listeners) {
            listeners.remove(listener);
        }
    }

}
