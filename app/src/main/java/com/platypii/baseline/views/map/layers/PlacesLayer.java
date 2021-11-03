package com.platypii.baseline.views.map.layers;

import com.platypii.baseline.Services;
import com.platypii.baseline.databinding.MapInfoWindowBinding;
import com.platypii.baseline.places.Place;
import com.platypii.baseline.views.map.PlaceIcons;

import android.view.LayoutInflater;
import android.view.View;
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
    private final LayoutInflater inflater;
    @NonNull
    private final Map<Place, Marker> placeMarkers = new HashMap<>();

    public PlacesLayer(@NonNull LayoutInflater inflater) {
        this.inflater = inflater;
    }

    @Override
    public void onAdd(@NonNull GoogleMap map) {
        this.map = map;
        map.setInfoWindowAdapter(new PlaceInfoWindow());
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
                    .snippet(place.snippet())
            );
            placeMarkers.put(place, placeMarker);
        }
    }

    private class PlaceInfoWindow implements GoogleMap.InfoWindowAdapter {
        @Nullable
        @Override
        public View getInfoContents(@NonNull Marker marker) {
            final String title = marker.getTitle();
            if (title != null && !title.isEmpty()) {
                final MapInfoWindowBinding binding = MapInfoWindowBinding.inflate(inflater);
                binding.infoTitle.setText(title);
                binding.infoSnippet.setText(marker.getSnippet());
                return binding.getRoot();
            } else {
                return null;
            }
        }

        @Nullable
        @Override
        public View getInfoWindow(@NonNull Marker marker) {
            return null;
        }
    }

}
