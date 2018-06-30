package com.platypii.baseline.measurements;

/**
 * Copies an android SensorEvent
 */
public class MGravity extends MSensor {

    public MGravity(long nano, float x, float y, float z) {
        this.nano = nano;
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
        // millis,nano,sensor,pressure,lat,lon,hMSL,velN,velE,numSV,gX,gY,gZ,rotX,rotY,rotZ,acc
        return "," + nano + ",grv,,,,,,,," + gX + "," + gY + "," + gZ;
    }

}
