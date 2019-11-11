package com.platypii.baseline.views.map;

import com.platypii.baseline.Services;
import com.platypii.baseline.places.Place;

import androidx.annotation.NonNull;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

class NearestPlaceLayer implements MapLayer {

    private final Marker placeMarker;

    NearestPlaceLayer(@NonNull GoogleMap map) {
        placeMarker = map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .visible(false)
                .alpha(0.5f)
                .anchor(0.5f, 1f)
                .flat(true)
        );
    }

    @Override
    public void update() {
        if (Services.location.lastLoc != null) {
            final Place place = Services.places.nearestPlace.cached(Services.location.lastLoc);
            if (place != null) {
                placeMarker.setVisible(true);
                placeMarker.setPosition(place.latLng());
                placeMarker.setIcon(PlaceIcons.icon(place));
            }
        }
    }

}
