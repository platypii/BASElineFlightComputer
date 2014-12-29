package com.platypii.baseline.data;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;


public class MySensorListener implements SensorEventListener {

	public final int sensor_type;
	
	// History
	private static final int maxHistory = 300; // Maximum number of measurements to keep in memory
	public final SyncedList<MySensorEvent> history = new SyncedList<>(maxHistory);
	public MySensorEvent lastSensorEvent;
	
	
	public MySensorListener(int type) {
		this.sensor_type = type;
	}
	
	public void onAccuracyChanged(Sensor sensor, int accuracy) {}
	public void onSensorChanged(SensorEvent event) {
    	// Store sensor event
		lastSensorEvent = new MySensorEvent(event);
		history.addLast(lastSensorEvent);
	}
}
