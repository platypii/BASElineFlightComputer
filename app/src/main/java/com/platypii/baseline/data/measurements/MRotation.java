package com.platypii.baseline.data.measurements;

/** Copies an android SensorEvent */
public class MRotation extends MSensor {

    public MRotation(long nano, float x, float y, float z) {
        this.nano = nano;
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
        // millis, nano, sensor, pressure, latitude, longitude, altitude_gps, vN, vE, satellites, gX, gY, gZ, rotX, rotY, rotZ, acc
        return String.format(",%d,rot,,,,,,,,,,,%f,%f,%f,", nano, rotX, rotY, rotZ);
    }

}
