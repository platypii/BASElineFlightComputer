package com.platypii.baseline.data.measurements;

/** Copies an android SensorEvent */
public class MRotation extends MSensor {

    public MRotation(long millis, float x, float y, float z) {
        this.timeMillis = millis;
        // this.accuracy = event.accuracy;
        this.rotX = x;
        this.rotY = y;
        this.rotZ = z;
    }

    public float x() {
        return rotX;
    }
    public float y() {
        return rotY;
    }
    public float z() {
        return rotZ;
    }

    @Override
    public String toRow() {
        // timeMillis, sensor, altitude, climb, pressure, latitude, longitude, altitude_gps, vN, vE, satellites, gX, gY, gZ, rotX, rotY, rotZ, acc
        return String.format("%d,rot,,,,,,,,,,,,,%f,%f,%f,", timeMillis, rotX, rotY, rotZ);
    }

}
