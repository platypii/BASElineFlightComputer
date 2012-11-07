package com.platypii.baseline.data;


/**
 * An altitude measurement
 * @author platypii
 */
public class MyAltitude extends Measurement {


    public MyAltitude(long timeMillis, double altitude, double climb,
    				   float pressure, double altitude_gps) {

        // Load state data (flightMode, orientation, etc)
        this.loadState();

    	this.timeMillis = timeMillis;
        this.sensor = "Alti";
        this.altitude = altitude;
        this.climb = climb;
        this.pressure = pressure;
        this.altitude_gps = altitude_gps;
    }

}
