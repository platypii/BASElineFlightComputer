package com.platypii.baseline.data.measurements;

/** Copies an android SensorEvent */
public class MGravity extends MSensor {

    public MGravity(long millis, float x, float y, float z) {
        this.timeMillis = millis;
        // this.accuracy = event.accuracy;
        this.gX = x;
        this.gY = y;
        this.gZ = z;
    }

    public float x() {
        return gX;
    }
    public float y() {
        return gY;
    }
    public float z() {
        return gZ;
    }

    @Override
    public String toRow() {
        // timeMillis, sensor, altitude, climb, pressure, latitude, longitude, altitude_gps, vN, vE, satellites, gX, gY, gZ, rotX, rotY, rotZ, acc
        return String.format("%d,grv,,,,,,,,,,%f,%f,%f,,,", timeMillis, gX, gY, gZ);
    }

}
