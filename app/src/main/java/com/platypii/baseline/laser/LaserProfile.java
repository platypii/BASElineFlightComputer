package com.platypii.baseline.laser;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.gson.annotations.SerializedName;
import com.platypii.baseline.places.Place;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Numbers;
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

    @NonNull
    public String locationString() {
        if (isReal(lat) && isReal(lng) && isReal(alt)) {
            return Numbers.format6.format(lat) + ", " + Numbers.format6.format(lng) + ", " + Convert.distance(alt);
        } else if (isReal(alt)) {
            return Convert.distance(alt);
        } else {
            return "";
        }
    }

    public boolean isLocal() {
        return laser_id.startsWith("tmp-");
    }

    private boolean isReal(@Nullable Double value) {
        return value != null && !Double.isNaN(value) && !Double.isInfinite(value);
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
