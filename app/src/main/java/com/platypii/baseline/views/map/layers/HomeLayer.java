package com.platypii.baseline.views.map.layers;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.location.LandingZone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import java.util.ArrayList;
import java.util.List;

public class HomeLayer extends MapLayer {

    @Nullable
    private Marker homeMarker;
    @Nullable
    private Polyline homePath;
    private final List<LatLng> homePoints = new ArrayList<>();

    @Override
    public void onAdd(@NonNull GoogleMap map) {
        // Add home location pin
        homeMarker = map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .visible(false)
                .title("home")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin))
                .anchor(0.5f, 1.0f)
        );
        // Add line to home
        homePath = map.addPolyline(new PolylineOptions()
                .visible(false)
                .width(10)
                .color(0x66ffffff)
                .startCap(new RoundCap())
                .endCap(new RoundCap())
        );
    }

    @Override
    public void update() {
        if (homeMarker != null && homePath != null) {
            final LatLng home = LandingZone.homeLoc;
            if (home != null) {
                homeMarker.setPosition(home);
                homeMarker.setVisible(true);
                if (Services.location.lastLoc != null) {
                    final LatLng currentLoc = Services.location.lastLoc.latLng();
                    homePoints.clear();
                    homePoints.add(currentLoc);
                    homePoints.add(LandingZone.homeLoc);
                    homePath.setPoints(homePoints);
                    homePath.setVisible(true);
                }
            } else {
                homeMarker.setVisible(false);
                homePath.setVisible(false);
            }
        }
    }

}
