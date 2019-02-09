package com.platypii.baseline.laser;

public class GeoPoint {

    public long millis;
    public double alt;
    public double lat;
    public double lng;

    public GeoPoint(long millis, double alt, double lat, double lng) {
        this.millis = millis;
        this.alt = alt;
        this.lat = lat;
        this.lng = lng;
    }
}
