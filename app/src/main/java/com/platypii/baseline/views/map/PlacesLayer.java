package com.platypii.baseline.views.map;

import com.platypii.baseline.Services;
import com.platypii.baseline.places.Place;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

class PlacesLayer implements MapLayer {

    private final Marker placeMarker;

    PlacesLayer(GoogleMap map) {
        placeMarker = map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .visible(false)
                .alpha(0.5f)
                .anchor(0.5f, 0.5f)
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
                placeMarker.setIcon(PlaceIcons.icon(place.objectType));
            }
        }
    }

}
