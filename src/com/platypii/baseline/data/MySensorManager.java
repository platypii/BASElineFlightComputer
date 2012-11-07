package com.platypii.baseline.data;

import java.util.List;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;


public class MySensorManager {
    
    // Singleton MySensorManager
    private static MySensorManager _instance;
    
    // Android sensor manager (accelerometer, gravity, gyro, linear accel, magnetic, pressure, humidity, rotation, temp)
    private static SensorManager sensorManager;

    // History
    // History is a map from sensor type to their history
    // public static HashMap<Integer,MySensorListener> listeners = new HashMap<Integer,MySensorListener>();
    // public static HashMap<Integer,LinkedList<MySensorEvent>> history = new HashMap<Integer,LinkedList<MySensorEvent>>();
    // public static HashMap<Integer, TreeMap<Long,MySensorEvent>> history;
    public static MySensorListener accel = null;
    public static MySensorListener gravity = null;
    public static MySensorListener rotation = null;
    
    
    /**
     * Initializes location services
     * 
     * @param theContext The Application context
     */
    public static synchronized void initSensors(Context appContext) {
        if(_instance == null) {
            _instance = new MySensorManager();
        
            sensorManager = (SensorManager)appContext.getSystemService(Context.SENSOR_SERVICE);
    
            // Sensors
            // barometer = startSensor(Sensor.TYPE_PRESSURE);
            accel = startSensor(Sensor.TYPE_ACCELEROMETER);
            
            if(hasSensor(Sensor.TYPE_GRAVITY))
            	gravity = startSensor(Sensor.TYPE_GRAVITY);
            else
            	gravity = accel;
            
            if(hasSensor(Sensor.TYPE_ROTATION_VECTOR))
                rotation = startSensor(Sensor.TYPE_ROTATION_VECTOR);
            else if(hasSensor(Sensor.TYPE_MAGNETIC_FIELD))
                rotation = startSensor(Sensor.TYPE_MAGNETIC_FIELD);
    
//          startSensor(Sensor.TYPE_LINEAR_ACCELERATION);
//          startSensor(Sensor.TYPE_GYROSCOPE);
//          startSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
//          startSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        }
    }
    
    /**
     * Starts registering events for a given sensor
     */
    private static MySensorListener startSensor(int type) {
        Sensor sensor = sensorManager.getDefaultSensor(type);
        MySensorListener listener = new MySensorListener(type);
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_FASTEST);
        return listener;
    }
    
    public static boolean hasSensor(int type) {
        return sensorManager.getDefaultSensor(type) != null;
    }
    
    /**
     * Returns a string representation of all available sensors
     */
    public static CharSequence getSensorsString() {
        StringBuffer buffer = new StringBuffer();
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        for(Sensor sensor : sensors) {
            buffer.append(sensor.getVendor() + " - " + sensor.getName());
            buffer.append('\n');
        }
        return buffer;
    }
        
}
