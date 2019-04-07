package com.platypii.baseline.laser;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LaserProfile {
    public final String laser_id;
    @Nullable
    public final String user_id;
    public final String name;
    @SerializedName("public")
    public final boolean isPublic;
    public final Double alt;
    public final Double lat;
    public final Double lng;
    public final String source;
    public final List<LaserMeasurement> points;

    public LaserProfile(String laser_id, @Nullable String user_id, String name, boolean isPublic, Double alt, Double lat, Double lng, String source, List<LaserMeasurement> points) {
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

    @NonNull
    @Override
    public String toString() {
        return name;
    }

}
