package com.platypii.baseline.places;

import android.support.annotation.NonNull;
import com.google.android.gms.maps.model.LatLng;

/**
 * Represents a known jumping location
 */
public class Place {

    public final String name;
    public final String region;
    public final String country;

    public final double latitude;
    public final double longitude;
    public final double altitude;

    // B,A,S,E,DZ
    public final String objectType;

    public final double radius;

    Place(String name, String region, String country, double latitude, double longitude, double altitude, String objectType, double radius) {
        this.name = name;
        this.region = region;
        this.country = country;
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
        this.objectType = objectType;
        this.radius = radius;
    }

    private LatLng lazyLatLng = null;
    @NonNull
    public LatLng latLng() {
        if (lazyLatLng == null) {
            lazyLatLng = new LatLng(latitude, longitude);
        }
        return lazyLatLng;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

}
