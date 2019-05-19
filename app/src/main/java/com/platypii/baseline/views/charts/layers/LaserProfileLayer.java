package com.platypii.baseline.views.charts.layers;

import com.platypii.baseline.laser.LaserMeasurement;
import com.platypii.baseline.laser.LaserProfile;

import androidx.annotation.NonNull;
import java.util.List;

public class LaserProfileLayer extends ProfileLayer {

    @NonNull
    public LaserProfile laserProfile;

    public LaserProfileLayer(@NonNull LaserProfile laserProfile) {
        super(Colors.nextColor());
        this.laserProfile = laserProfile;
        loadLaser(laserProfile);
    }

    @NonNull
    @Override
    public String id() {
        return laserProfile.laser_id;
    }

    @NonNull
    @Override
    public String name() {
        return laserProfile.name;
    }

    public void loadLaser(@NonNull LaserProfile laserProfile) {
        this.laserProfile = laserProfile;
        // Reorder to handle measuring from bottom
        final List<LaserMeasurement> points = LaserMeasurement.reorder(laserProfile.points);
        // Load laser data into time series
        profileSeries.reset();
        profileSeries.addPoint(0, 0);
        for (LaserMeasurement point : points) {
            profileSeries.addPoint(point.x, point.y);
        }
    }

}
