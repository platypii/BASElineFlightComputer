package com.platypii.baseline.views.map.layers;

import com.platypii.baseline.Services;
import com.platypii.baseline.places.Place;
import com.platypii.baseline.views.map.PlaceIcons;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

class NearestPlaceLayer extends MapLayer {

    @Nullable
    private Marker placeMarker;

    @Override
    public void onAdd(@NonNull GoogleMap map) {
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
        if (placeMarker != null && Services.location.lastLoc != null) {
            final Place place = Services.places.nearestPlace.cached(Services.location.lastLoc);
            if (place != null) {
                placeMarker.setVisible(true);
                placeMarker.setPosition(place.latLng());
                placeMarker.setIcon(PlaceIcons.icon(place));
            }
        }
    }

}
