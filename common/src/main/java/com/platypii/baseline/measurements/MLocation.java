package com.platypii.baseline.measurements;

import com.platypii.baseline.location.Geo;
import com.platypii.baseline.location.LocationCheck;
import com.platypii.baseline.location.NMEAException;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.util.Numbers;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import java.util.Locale;

public class MLocation extends Measurement {
    private static final String TAG = "MLocation";

    // GPS
    public final double latitude; // Latitude
    public final double longitude; // Longitude
    public final double altitude_gps; // GPS altitude MSL
    private final double vN; // Velocity north
    private final double vE; // Velocity east
    public float hAcc = Float.NaN; // Horizontal accuracy
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
        if(locationError != LocationCheck.VALID) {
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

    @Override
    public String toRow() {
        final String sat_str = (satellitesUsed != -1)? Integer.toString(satellitesUsed) : "";
        final String vN_str = Numbers.isReal(vN)? Double.toString(vN) : "";
        final String vE_str = Numbers.isReal(vE)? Double.toString(vE) : "";
        // millis,nano,sensor,pressure,lat,lon,hMSL,velN,velE,numSV,gX,gY,gZ,rotX,rotY,rotZ,acc
        return String.format(Locale.US, "%d,,gps,,%f,%f,%f,%s,%s,%s", millis, latitude, longitude, altitude_gps, vN_str, vE_str, sat_str);
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "MLocation(%d,%.6f,%.6f,%.1f,%.0f,%.0f)", millis, latitude, longitude, altitude_gps, vN, vE);
    }

    public double groundSpeed() {
        return Math.sqrt(vN * vN + vE * vE);
    }

    public double totalSpeed() {
        if(!Double.isNaN(climb)) {
            return Math.sqrt(vN * vN + vE * vE + climb * climb);
        } else {
            // If we don't have altimeter data, fall back to ground speed
            return Math.sqrt(vN * vN + vE * vE);
        }
    }

    public double glideRatio() {
        return - groundSpeed() / climb;
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
    public LatLng moveDirection(double bearing, double distance) {
        return Geo.moveDirection(latitude, longitude, bearing, distance);
    }

}
