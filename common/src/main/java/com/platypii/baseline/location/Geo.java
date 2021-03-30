package com.platypii.baseline.location;

import androidx.annotation.NonNull;
import com.google.android.gms.maps.model.LatLng;

/**
 * Geographic helpers
 */
public class Geo {

    private static final double R = 6371000; // meters

    /**
     * Computes the distance between two points
     *
     * @return the distance in meters
     */
    public static double distance(double lat1, double lon1, double lat2, double lon2) {
        final double lat1r = Math.toRadians(lat1);
        final double lat2r = Math.toRadians(lat2);
        final double delta_lat = lat2r - lat1r;
        final double delta_lon = Math.toRadians(lon2 - lon1);

        final double sin_lat = Math.sin(delta_lat / 2);
        final double sin_lon = Math.sin(delta_lon / 2);

        // Haversine formula
        final double a = sin_lat * sin_lat + Math.cos(lat1r) * Math.cos(lat2r) * sin_lon * sin_lon;
        final double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }

    /**
     * Computes the approximate distance between two points.
     * Assumes equirectangular earth, adjusted by latitude.
     * Much faster to compute than haversine.
     *
     * @return the distance in meters
     */
    public static double fastDistance(double lat1, double lon1, double lat2, double lon2) {
        final double lat1r = Math.toRadians(lat1);
        final double lat2r = Math.toRadians(lat2);
        final double delta_lon = Math.toRadians(lon2 - lon1);

        final double x = delta_lon * Math.cos((lat1r + lat2r) / 2);
        final double y = lat2r - lat1r;

        return R * Math.sqrt(x * x + y * y);
    }

    /**
     * Computes the bearing from location1 to location2
     *
     * @return the bearing in degrees (relative to true north, not magnetic)
     */
    public static double bearing(double lat1, double lon1, double lat2, double lon2) {
        final double lat1r = Math.toRadians(lat1);
        final double lat2r = Math.toRadians(lat2);
        final double delta_lon = Math.toRadians(lon2 - lon1);
        final double y = Math.sin(delta_lon) * Math.cos(lat2r);
        final double x = Math.cos(lat1r) * Math.sin(lat2r) - Math.sin(lat1r) * Math.cos(lat2r) * Math.cos(delta_lon);
        return Math.toDegrees(Math.atan2(y, x));
    }

    /**
     * Moves the location along a bearing (degrees) by a given distance (meters)
     */
    @NonNull
    public static LatLng moveBearing(double latitude, double longitude, double bearing, double distance) {
        final double d = distance / R;

        final double lat = Math.toRadians(latitude);
        final double lon = Math.toRadians(longitude);
        final double bear = Math.toRadians(bearing);

        // Precompute trig
        final double sin_d = Math.sin(d);
        final double cos_d = Math.cos(d);
        final double sin_lat = Math.sin(lat);
        final double cos_lat = Math.cos(lat);
        final double sin_d_cos_lat = sin_d * cos_lat;

        final double lat2 = Math.asin(sin_lat * cos_d + sin_d_cos_lat * Math.cos(bear));
        final double lon2 = lon + Math.atan2(Math.sin(bear) * sin_d_cos_lat, cos_d - sin_lat * Math.sin(lat2));

        final double lat3 = Math.toDegrees(lat2);
        final double lon3 = mod360(Math.toDegrees(lon2));

        return new LatLng(lat3, lon3);
    }

    // Helpers
    private static double mod360(double degrees) {
        return ((degrees + 540) % 360) - 180;
    }

}
