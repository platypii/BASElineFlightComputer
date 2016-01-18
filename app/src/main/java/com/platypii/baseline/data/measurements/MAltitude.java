package com.platypii.baseline.data.measurements;


/**
 * An altitude measurement
 * @author platypii
 */
public class MAltitude extends Measurement {
    public String sensor = "Alt";

    public MAltitude(long nano, double altitude, double climb, float pressure, double altitude_gps) {
        this.nano = nano;
        this.sensor = "Alt";
        this.altitude = altitude;
        this.climb = climb;
        this.pressure = pressure;
    }

    @Override
    public String toRow() {
        // millis,nano,sensor,pressure,lat,lon,hMSL,velN,velE,numSV,gX,gY,gZ,rotX,rotY,rotZ,acc
        return String.format(",%d,alt,%f", nano, pressure);
    }

}
