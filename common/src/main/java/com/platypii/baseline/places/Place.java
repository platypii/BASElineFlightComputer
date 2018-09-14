package com.platypii.baseline.places;

import com.google.android.gms.maps.model.LatLng;

/**
 * Represents a known jumping location
 */
public class Place {

    public String name;
    public String region;
    public String country;

    public double latitude;
    public double longitude;
    public double altitude;

    // B,A,S,E,DZ
    public String objectType;

    public Place(String name, String region, String country, double latitude, double longitude, double altitude, String objectType) {
        this.name = name;
        this.region = region;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.objectType = objectType;
    }

    public LatLng latLng() {
        return new LatLng(latitude, longitude);
    }

    @Override
    public String toString() {
        return name;
    }

}
