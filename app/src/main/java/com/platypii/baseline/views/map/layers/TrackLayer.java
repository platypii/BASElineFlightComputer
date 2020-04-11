package com.platypii.baseline.views.map.layers;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.charts.layers.Colors;

import androidx.annotation.NonNull;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.PolylineOptions;

public class TrackLayer extends MapLayer {

    @NonNull
    private final TrackData trackData;

    public TrackLayer(@NonNull TrackData trackData) {
        this.trackData = trackData;
    }

    @Override
    public void onAdd(@NonNull GoogleMap map) {
        final PolylineOptions plane = new PolylineOptions().color(Colors.modePlane);
        final PolylineOptions flight = new PolylineOptions().color(Colors.modeWingsuit);
        final PolylineOptions canopy = new PolylineOptions().color(Colors.modeCanopy);
        final PolylineOptions ground = new PolylineOptions().color(Colors.modeGround);
        for (MLocation loc : trackData.data) {
            if (loc == null) {
                Exceptions.report(new NullPointerException("Unexpected null location for track " + trackData));
                continue;
            }

            if (trackData.stats.exit != null && trackData.stats.deploy != null && trackData.stats.land != null) {
                if (loc.millis <= trackData.stats.exit.millis) {
                    plane.add(loc.latLng());
                }
                if (trackData.stats.exit.millis <= loc.millis && loc.millis <= trackData.stats.deploy.millis) {
                    flight.add(loc.latLng());
                }
                if (trackData.stats.deploy.millis <= loc.millis && loc.millis <= trackData.stats.land.millis) {
                    canopy.add(loc.latLng());
                }
                if (trackData.stats.land.millis <= loc.millis) {
                    ground.add(loc.latLng());
                }
            } else {
                flight.add(loc.latLng());
            }
        }
        map.addPolyline(ground);
        map.addPolyline(canopy);
        map.addPolyline(flight);
        map.addPolyline(plane);
    }

    @Override
    public void update() {
    }
}
