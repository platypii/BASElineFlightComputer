package com.platypii.baseline.laser;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LaserProfile {
    public String laser_id;
    @Nullable
    public String user_id;
    public String name;
    @SerializedName("public")
    public boolean isPublic;
    public Double alt;
    public Double lat;
    public Double lng;
    public String source;
    public List<LaserMeasurement> points;

    public LaserProfile(String laser_id, String user_id, String name, boolean isPublic, Double alt, Double lat, Double lng, String source, List<LaserMeasurement> points) {
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
