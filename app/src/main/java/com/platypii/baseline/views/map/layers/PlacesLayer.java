package com.platypii.baseline.views.map.layers;

import com.platypii.baseline.Services;
import com.platypii.baseline.places.Place;
import com.platypii.baseline.views.map.PlaceIcons;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class PlacesLayer extends MapLayer {

    @Nullable
    private GoogleMap map;
    @NonNull
    private final Map<Place, Marker> placeMarkers = new HashMap<>();

    @Override
    public void onAdd(@NonNull GoogleMap map) {
        this.map = map;
    }

    @Override
    public void update() {
        if (map != null) {
            final LatLngBounds bounds = map.getProjection().getVisibleRegion().latLngBounds;
            final List<Place> places = Services.places.getPlacesByArea(bounds);
            // Remove out of bounds places
            final Iterator<Map.Entry<Place, Marker>> it = placeMarkers.entrySet().iterator();
            while (it.hasNext()) {
                final Map.Entry<Place, Marker> entry = it.next();
                if (!places.contains(entry.getKey())) {
                    // Remove from HashMap and GoogleMap
                    entry.getValue().remove();
                    it.remove();
                }
            }
            // Add new places
            for (Place place : places) {
                if (!placeMarkers.containsKey(place)) {
                    addMarker(place);
                }
            }
        }
    }

    private void addMarker(@NonNull Place place) {
        if (map != null) {
            final Marker placeMarker = map.addMarker(new MarkerOptions()
                    .position(place.latLng())
                    .visible(true)
                    .alpha(0.5f)
                    .anchor(0.5f, 1f)
                    .flat(true)
                    .icon(PlaceIcons.icon(place))
                    .title(place.shortName())
            );
            placeMarkers.put(place, placeMarker);
        }
    }

}
