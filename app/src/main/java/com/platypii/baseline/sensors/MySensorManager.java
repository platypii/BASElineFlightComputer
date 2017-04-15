package com.platypii.baseline.sensors;

import com.platypii.baseline.Service;
import com.platypii.baseline.measurements.MAccel;
import com.platypii.baseline.measurements.MGravity;
import com.platypii.baseline.measurements.MRotation;
import com.platypii.baseline.measurements.MSensor;
import com.platypii.baseline.util.SyncedList;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Service to manage orientation sensors, and listeners
 * accelerometer, gravity, gyro, linear accel, magnetic, pressure, humidity, rotation, temp
 *
 * TODO: Do we care about gravity sensor? Or would we be better off with accel + rotation?
 */
public class MySensorManager implements SensorEventListener, Service {
    private static final String TAG = "MySensorManager";

    private SensorManager sensorManager;

    // History
    public final SyncedList<MSensor> accel = new SyncedList<>();
    public final SyncedList<MSensor> gravity = new SyncedList<>();
    public final SyncedList<MSensor> rotation = new SyncedList<>();

    private final List<MySensorListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Initialize orientation sensor services
     *
     * @param context The Application context
     */
    @Override
    public void start(@NonNull final Context context) {
        Log.i(TAG, "Starting sensor manager");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                // Get android sensor manager
                sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
                // Find sensors
                final Sensor accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                final Sensor gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
                final Sensor rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
                // Register listeners
                sensorManager.registerListener(MySensorManager.this, accelSensor, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(MySensorManager.this, gravitySensor, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(MySensorManager.this, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST);
            }
        });
    }

    /** SensorEventListener */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    @Override
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

//    /**
//     * Returns a string representation of all available sensors
//     */
//    public CharSequence getSensorsString() {
//        final StringBuffer buffer = new StringBuffer();
//        final List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
//        for(Sensor sensor : sensors) {
//            buffer.append(sensor.getVendor());
//            buffer.append(" - ");
//            buffer.append(sensor.getName());
//            buffer.append('\n');
//        }
//        return buffer;
//    }

    @Override
    public void stop() {
        if(sensorManager != null) {
            sensorManager.unregisterListener(this);
            sensorManager = null;
        } else {
            Log.e(TAG, "Sensor manager already stopped");
        }
        if(!listeners.isEmpty()) {
            Log.e(TAG, "Stopping sensor service, but listeners are still listening");
        }
    }

    /**
     * Add a new listener to be notified of location updates
     */
    public void addListener(MySensorListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove a listener from location updates
     */
    public void removeListener(MySensorListener listener) {
        listeners.remove(listener);
    }

}
