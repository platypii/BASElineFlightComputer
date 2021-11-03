package com.platypii.baseline.measurements;

import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Numbers;

import androidx.annotation.NonNull;

import static com.platypii.baseline.util.Numbers.isReal;

public class LatLngAlt {

    public final double lat;
    public final double lng;
    public final double alt;

    public LatLngAlt(double lat, double lng, double alt) {
        this.lat = lat;
        this.lng = lng;
        this.alt = alt;
    }

    @NonNull
    @Override
    public String toString() {
        return formatLatLngAlt(lat, lng, alt);
    }

    @NonNull
    public static String formatLatLng(double lat, double lng) {
        if (isReal(lat) && isReal(lng)) {
            return Numbers.format6.format(lat) + ", " + Numbers.format6.format(lng);
        } else {
            return "";
        }
    }

    @NonNull
    public static String formatLatLngAlt(double lat, double lng, double alt) {
        if (isReal(lat) && isReal(lng) && isReal(alt)) {
            return Numbers.format6.format(lat) + ", " + Numbers.format6.format(lng) + ", " + Convert.distance(alt);
        } else if (isReal(alt)) {
            return Convert.distance(alt);
        } else {
            return "";
        }
    }
}
