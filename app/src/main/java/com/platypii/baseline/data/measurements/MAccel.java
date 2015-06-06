package com.platypii.baseline.data.measurements;

/** Copies an android SensorEvent */
public class MAccel extends MSensor {

    public MAccel(long millis, float a) {
        this.timeMillis = millis;
        // this.accuracy = event.accuracy;
        this.acc = a;
    }

    public float x() {
        return acc;
    }
    public float y() {
        return Float.NaN;
    }
    public float z() {
        return Float.NaN;
    }

    @Override
    public String toRow() {
        // millis, sensor, pressure, latitude, longitude, altitude_gps, vN, vE, satellites, gX, gY, gZ, rotX, rotY, rotZ, acc
        return String.format("%d,acc,,,,,,,,,,,,,,%f", timeMillis, acc);
    }

}
