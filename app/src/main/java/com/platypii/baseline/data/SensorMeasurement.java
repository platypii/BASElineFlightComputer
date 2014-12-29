package com.platypii.baseline.data;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.util.Log;


// Copies an android SensorEvent
public class SensorMeasurement extends Measurement {

    public SensorMeasurement(SensorEvent event) {

        // Load state data (flightMode, orientation, etc)
        this.loadState();

        // Store sensor data
    	this.timeMillis = event.timestamp;
        this.sensor = event.sensor.getName();
        // this.accuracy = event.accuracy;
        if(event.sensor.getType() == Sensor.TYPE_GRAVITY) {
            assert event.values.length == 3;
            this.gX = event.values[0];
            this.gY = event.values[1];
            this.gZ = event.values[2];
        } else if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            assert event.values.length == 3;
            this.rotX = event.values[0];
            this.rotY = event.values[1];
            this.rotZ = event.values[2];
        } else {
        	// Generic
        	Log.e("SensorMeasurement", "Unknown sensor type");
//	        assert event.values != null;
//	        assert event.values.length >= 1;
//	        this.x = event.values[0];
//	        if(event.values.length >= 2)
//	            this.y = event.values[1];
//	        if(event.values.length >= 3)
//	            this.z = event.values[2];
        }
    }

}