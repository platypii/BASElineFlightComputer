package com.platypii.baseline.laser;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Numbers;
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
    public String locationString() {
        if (isReal(lat) && isReal(lng)) {
            return Numbers.format6.format(lat) + ", " + Numbers.format6.format(lng) + ", " + Convert.distance(alt);
        } else if (isReal(alt)) {
            return Convert.distance(alt);
        } else {
            return "";
        }
    }

    private boolean isReal(Double value) {
        return value != null && !Double.isNaN(value) && !Double.isInfinite(value);
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

}
