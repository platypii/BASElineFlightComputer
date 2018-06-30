package com.platypii.baseline.measurements;

/**
 * Copies an android SensorEvent
 */
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
        // millis,nano,sensor,pressure,lat,lon,hMSL,velN,velE,numSV,gX,gY,gZ,rotX,rotY,rotZ,acc
        return "," + nano + ",rot,,,,,,,,,,," + rotX + "," + rotY + "," + rotZ;
    }

}
