package com.platypii.baseline.laser;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LaserProfile {
    public String laser_id;
    public String name;
    @SerializedName("public")
    public boolean isPublic;
    @Nullable
    public GeoPoint exit;
    public String source;
    public List<LaserMeasurement> points;

    public LaserProfile(String laser_id, String name, boolean isPublic, GeoPoint exit, String source, List<LaserMeasurement> points) {
        this.laser_id = laser_id;
        this.name = name;
        this.isPublic = isPublic;
        this.exit = exit;
        this.source = source;
        this.points = points;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

}
