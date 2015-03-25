package com.platypii.baseline.data.measurements;


/**
 * An altitude measurement
 * @author platypii
 */
public class MAltitude extends Measurement {
    public String sensor = "Alt";

    public MAltitude(long timeMillis, double altitude, double climb, float pressure, double altitude_gps) {
    	this.timeMillis = timeMillis;
        this.sensor = "Alt";
        this.altitude = altitude;
        this.climb = climb;
        this.pressure = pressure;
    }

    @Override
    public String toRow() {
        // timeMillis, sensor, altitude, climb, pressure, latitude, longitude, altitude_gps, gX, gY, gZ, rotX, rotY, rotZ, acc
        return String.format("%d,alt,%f,%f,%f", timeMillis, altitude, climb, pressure);
    }

}
