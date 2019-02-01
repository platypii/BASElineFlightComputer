package com.platypii.baseline.laser;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LaserProfile {
    public String laser_id;
    public String name;
    @SerializedName("public")
    public boolean isPublic;
    public String source;
    public List<LaserMeasurement> points;

    public LaserProfile(String laser_id, String name, boolean isPublic, String source, List<LaserMeasurement> points) {
        this.laser_id = laser_id;
        this.name = name;
        this.isPublic = isPublic;
        this.source = source;
        this.points = points;
    }

}
