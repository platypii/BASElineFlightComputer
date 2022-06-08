package com.platypii.baseline.views.map;

import com.platypii.baseline.views.map.layers.HomeLayer;
import com.platypii.baseline.views.map.layers.LandingLayer;
import com.platypii.baseline.views.map.layers.MyPositionLayer;
import com.platypii.baseline.views.map.layers.PlacesLayer;

import androidx.annotation.NonNull;
import com.google.android.gms.maps.GoogleMap;

/**
 * Main map fragment
 */
public class MapFragment extends BaseMapFragment {
    private static final String TAG = "Map";

    public PlacesLayer placesLayer;

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        super.onMapReady(map);

        // Add map layers
        addLayer(placesLayer = new PlacesLayer(getLayoutInflater()));
        addLayer(new HomeLayer());
        addLayer(new LandingLayer());
        addLayer(new MyPositionLayer());
        updateLayers();
    }

}
