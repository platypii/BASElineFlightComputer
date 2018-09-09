package com.platypii.baseline.places;

import com.platypii.baseline.views.map.PlaceIcons;
import com.google.android.gms.maps.model.BitmapDescriptor;
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

    public BitmapDescriptor icon() {
        switch (objectType) {
            case "B": return PlaceIcons.b;
            case "A": return PlaceIcons.a;
            case "S": return PlaceIcons.s;
            case "E": return PlaceIcons.e;
            case "DZ": return PlaceIcons.dz;
            default: return PlaceIcons.other;
        }
    }

}
