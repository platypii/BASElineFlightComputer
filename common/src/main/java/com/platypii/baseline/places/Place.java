package com.platypii.baseline.places;

import com.platypii.baseline.measurements.LatLngAlt;
import com.platypii.baseline.util.Convert;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.model.LatLng;

/**
 * Represents a known jumping location
 */
public class Place {

    public final String name;
    public final String region;
    public final String country;

    public final double lat;
    public final double lng;
    public final double alt;

    // B,A,S,E,DZ
    public final String objectType;
    public final boolean wingsuitable;

    public final double radius;

    public Place(String name, String region, String country, double lat, double lng, double alt, String objectType, double radius, boolean wingsuitable) {
        this.name = name;
        this.region = region;
        this.country = country;
        this.lat = lat;
        this.lng = lng;
        this.alt = alt;
        this.objectType = objectType;
        this.radius = radius;
        this.wingsuitable = wingsuitable;
    }

    public boolean isBASE() {
        return !objectType.isEmpty() && "BASEO".contains(objectType);
    }

    public boolean isSkydive() {
        return !objectType.isEmpty() && objectType.startsWith("DZ");
    }

    @Nullable
    private LatLng lazyLatLng = null;

    @NonNull
    public LatLng latLng() {
        if (lazyLatLng == null) {
            lazyLatLng = new LatLng(lat, lng);
        }
        return lazyLatLng;
    }

    @NonNull
    public String niceString() {
        if (name.isEmpty()) {
            return country;
        } else {
            return name + ", " + country;
        }
    }

    @NonNull
    public String shortName() {
        if (name.isEmpty()) {
            return country;
        } else {
            return name;
        }
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

}
