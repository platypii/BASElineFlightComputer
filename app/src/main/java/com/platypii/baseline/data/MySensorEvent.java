package com.platypii.baseline.data;

import android.hardware.SensorEvent;


public class MySensorEvent {
    public final long timeMillis;
    public final int sensor;
    public float x;
    public float y;
    public float z;
    
    /**
     * Copies an android sensor event
     */
    public MySensorEvent(SensorEvent event) {
        this.sensor = event.sensor.getType();
        this.timeMillis = event.timestamp;
        assert event != null;
        assert event.values != null;
        assert event.values.length >= 1;
        this.x = event.values[0];
        if(event.values.length >= 2)
            this.y = event.values[1];
        if(event.values.length >= 3)
            this.z = event.values[2];
    }
}
