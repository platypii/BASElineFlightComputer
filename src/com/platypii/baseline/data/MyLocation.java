package com.platypii.baseline.data;


import android.location.Location;


public class MyLocation extends Measurement {
    
    private Location loc; // android.location.Location for convenience

    
    public MyLocation(long timeMillis, double latitude, double longitude, double altitude_gps,
    				  double vN, double vE,
    				  float hAcc, float pdop, float hdop, float vdop,
    				  int numSat, float groundDistance) {
    	
        // Load state data (altimeter, flightMode, orientation, etc)
        this.altitude = MyAltimeter.altitude;
        this.climb = MyAltimeter.climb;
        this.loadState();

        assert !Double.isInfinite(vN);
        assert !Double.isInfinite(vE);

        // Store location data
        this.timeMillis = timeMillis;
        this.sensor = "GPS";
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude_gps = altitude_gps;
        this.vN = vN;
        this.vE = vE;
        this.hAcc = hAcc;
        this.pdop = pdop;
        this.hdop = hdop;
        this.vdop = vdop;
        this.numSat = numSat;
        this.groundDistance = groundDistance;
    }
    
    /**
     * Returns an android Location
     */
    public Location loc() {
    	if(loc == null)
    		loc = new Location("gps");
    	loc.setLatitude(latitude);
    	loc.setLongitude(longitude);
    	loc.setAltitude(altitude);
    	if(!Float.isNaN(hAcc))
    		loc.setAccuracy(hAcc);
    	return loc;
    }
    
    @Override
    public String toString() {
        return loc().toString();
    }

}
