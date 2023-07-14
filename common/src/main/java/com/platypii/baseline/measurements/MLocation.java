package com.platypii.baseline.measurements;

import com.platypii.baseline.location.Geo;
import com.platypii.baseline.location.LocationCheck;
import com.platypii.baseline.location.NMEAException;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.util.Numbers;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.maps.model.LatLng;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MLocation extends Measurement implements Comparable<MLocation> {
    private static final String TAG = "MLocation";

    // GPS
    public final double latitude; // Latitude
    public final double longitude; // Longitude
    public final double altitude_gps; // GPS altitude MSL
    private final double vN; // Velocity north
    private final double vE; // Velocity east
    public float hAcc = Float.NaN; // LatitHorizontal accuracy
    //public float vAcc = Float.NaN; // Vertical accuracy
    //public float sAcc = Float.NaN; // Speed accuracy
    public final float pdop; // Positional dilution of precision
    public final float hdop; // Horizontal dilution of precision
    public final float vdop; // Vertical dilution of precision
    public final int satellitesUsed; // Satellites used in fix
    public final int satellitesInView; // Satellites in view

    public final double climb;  // Rate of climb (m/s)

    public MLocation(long millis, double latitude, double longitude, double altitude_gps,
                     double climb, // Usually taken from altimeter
                     double vN, double vE,
                     float hAcc, float pdop, float hdop, float vdop,
                     int satellitesUsed, int satellitesInView) {

        // Sanity checks
        final int locationError = LocationCheck.validate(latitude, longitude);
        if (locationError != LocationCheck.VALID) {
            final String locationErrorMessage = LocationCheck.message[locationError] + ": " + latitude + "," + longitude;
            Log.e(TAG, locationErrorMessage);
            Exceptions.report(new NMEAException(locationErrorMessage));
        }

        // Store location data
        this.millis = millis;
        this.sensor = "GPS";
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude_gps = altitude_gps;
        this.climb = climb;
        this.vN = vN;
        this.vE = vE;
        this.hAcc = hAcc;
        this.pdop = pdop;
        this.hdop = hdop;
        this.vdop = vdop;
        this.satellitesUsed = satellitesUsed;
        this.satellitesInView = satellitesInView;
    }

    @NonNull
    @Override
    public String toRow() {
        // millis,nano,sensor,pressure,lat,lon,hMSL,velN,velE,numSV,gX,gY,gZ,rotX,rotY,rotZ,acc
        final StringBuilder sb = new StringBuilder();
        sb.append(millis);
        sb.append(",,gps,,");
        sb.append(Numbers.format6.format(latitude));
        sb.append(',');
        sb.append(Numbers.format6.format(longitude));
        sb.append(',');
        sb.append(Numbers.format3.format(altitude_gps));
        sb.append(',');
        if (Numbers.isReal(vN)) {
            sb.append(Numbers.format2.format(vN));
        }
        sb.append(',');
        if (Numbers.isReal(vE)) {
            sb.append(Numbers.format2.format(vE));
        }
        sb.append(',');
        if (satellitesUsed != -1) {
            sb.append(satellitesUsed);
        }
        return sb.toString();
    }

    public double groundSpeed() {
        return Math.sqrt(vN * vN + vE * vE);
    }

    public double totalSpeed() {
        if (!Double.isNaN(climb)) {
            return Math.sqrt(vN * vN + vE * vE + climb * climb);
        } else {
            // If we don't have altimeter data, fall back to ground speed
            return Math.sqrt(vN * vN + vE * vE);
        }
    }

    public double glideRatio() {
        return -groundSpeed() / climb;
    }

    public double glideAngle() {
        return Math.toDegrees(Math.atan2(climb, groundSpeed()));
    }

    public double bearing() {
        return Math.toDegrees(Math.atan2(vE, vN));
    }

    @NonNull
    public LatLng latLng() {
        return new LatLng(latitude, longitude);
    }

    public double bearingTo(@NonNull MLocation dest) {
        return Geo.bearing(latitude, longitude, dest.latitude, dest.longitude);
    }

    public double bearingTo(@NonNull LatLng dest) {
        return Geo.bearing(latitude, longitude, dest.latitude, dest.longitude);
    }

    public double distanceTo(@NonNull MLocation dest) {
        return Geo.distance(latitude, longitude, dest.latitude, dest.longitude);
    }

    public double distanceTo(@NonNull LatLng dest) {
        return Geo.distance(latitude, longitude, dest.latitude, dest.longitude);
    }

    /**
     * Moves the location along a bearing (degrees) by a given distance (meters)
     */
    @NonNull
    public LatLng moveBearing(double bearing, double distance) {
        return Geo.moveBearing(latitude, longitude, bearing, distance);
    }

    /**
     * Implement natural ordering on millis
     */
    @Override
    public int compareTo(@NonNull MLocation loc) {
        return Long.compare(millis, loc.millis);
    }

    public boolean equals(@NonNull MLocation loc) {
        return loc.millis == millis && loc.latitude == latitude && loc.longitude == longitude && loc.vN == vN && loc.vE == vE;
    }

    @NonNull
    @Override
    public String toString() {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        final String date = sdf.format(new Date(millis));
        return String.format(Locale.US, "MLocation(%s,%.6f,%.6f,%.1f,%.0f,%.0f)", date, latitude, longitude, altitude_gps, vN, vE);
    }

    /**
     * Dummy constructor, useful for binary search
     */
    public MLocation() {
        this.millis = -1L;
        this.latitude = Double.NaN;
        this.longitude = Double.NaN;
        this.altitude_gps = Double.NaN;
        this.climb = Double.NaN;
        this.vN = Double.NaN;
        this.vE = Double.NaN;
        this.pdop = Float.NaN;
        this.hdop = Float.NaN;
        this.vdop = Float.NaN;
        this.satellitesUsed = 0;
        this.satellitesInView = 0;
    }

}
