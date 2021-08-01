package com.platypii.baseline.lasers;

import com.platypii.baseline.measurements.LatLngAlt;
import com.platypii.baseline.places.Place;
import com.platypii.baseline.util.Range;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LaserProfile {
    @NonNull
    public String laser_id;
    @Nullable
    public String user_id;
    public String name;
    @SerializedName("public")
    public boolean isPublic;
    @Nullable
    public Double alt;
    @Nullable
    public Double lat;
    @Nullable
    public Double lng;
    @Nullable
    public Place place;
    @Keep
    @NonNull
    public String source;
    public List<LaserMeasurement> points;

    public LaserProfile(
            @NonNull String laser_id,
            @Nullable String user_id,
            String name,
            boolean isPublic,
            @Nullable Double alt, @Nullable Double lat, @Nullable Double lng,
            @NonNull String source,
            List<LaserMeasurement> points
    ) {
        this.laser_id = laser_id;
        this.user_id = user_id;
        this.name = name;
        this.isPublic = isPublic;
        this.alt = alt;
        this.lat = lat;
        this.lng = lng;
        this.source = source;
        this.points = points;
    }

    /**
     * Format the location lat, lng, alt. Empty string if not defined.
     */
    @NonNull
    public String locationString() {
        if (lat != null && lng != null && alt != null) {
            return LatLngAlt.formatLatLngAlt(lat, lng, alt);
        } else {
            return "";
        }
    }

    public boolean isLocal() {
        return laser_id.startsWith("tmp-");
    }

    /**
     * Quadrant 1: laser from bottom
     * Quadrant 2: default x,y
     * Quadrant 4: reversed y,x
     *
     * @return 0 if invalid
     */
    public int quadrant() {
        if (points.isEmpty()) {
            return 2;
        }
        // Find height and width range
        final Range xRange = new Range();
        final Range yRange = new Range();
        for (LaserMeasurement point : points) {
            xRange.expand(point.x);
            yRange.expand(point.y);
        }
        if (xRange.min >= 0 && yRange.max <= 0) {
            return 2; // default x,y
        } else if (xRange.max <= 0 && yRange.min >= 0) {
            return 4; // reversed y,x
        } else if (xRange.min >= 0 && yRange.min >= 0) {
            return 1; // laser from bottom
        } else {
            return 0; // invalid laser
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LaserProfile && ((LaserProfile) obj).laser_id.equals(laser_id);
    }

    @NonNull
    @Override
    public String toString() {
        return "LaserProfile(" + laser_id + ", " + name + ")";
    }

}
