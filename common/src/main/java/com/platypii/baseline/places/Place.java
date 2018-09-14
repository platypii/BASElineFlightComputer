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

    Place(String name, String region, String country, double latitude, double longitude, double altitude, String objectType) {
        this.name = name;
        this.region = region;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.objectType = objectType;
    }

    private LatLng lazyLatLng = null;
    public LatLng latLng() {
        if (lazyLatLng == null) {
            lazyLatLng = new LatLng(latitude, longitude);
        }
        return lazyLatLng;
    }

    @Override
    public String toString() {
        return name;
    }

}
