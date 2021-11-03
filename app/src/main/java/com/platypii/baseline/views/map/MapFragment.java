package com.platypii.baseline.views.map;

import com.platypii.baseline.Services;
import com.platypii.baseline.location.LandingZone;
import com.platypii.baseline.views.map.layers.HomeLayer;
import com.platypii.baseline.views.map.layers.LandingLayer;
import com.platypii.baseline.views.map.layers.MyPositionLayer;
import com.platypii.baseline.views.map.layers.PlacesLayer;

import android.util.Log;
import androidx.annotation.NonNull;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

/**
 * Main map fragment
 */
public class MapFragment extends BaseMapFragment {
    private static final String TAG = "Map";

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        super.onMapReady(map);

        // Center priority: current location, home location, default location
        if (Services.location.lastLoc != null) {
            final LatLng center = Services.location.lastLoc.latLng();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, MapOptions.getZoom()));
            Log.i(TAG, "Centering map on " + center);
        } else if (LandingZone.homeLoc != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LandingZone.homeLoc, MapOptions.defaultZoom));
            Log.w(TAG, "Centering map on home " + LandingZone.homeLoc);
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(MapOptions.defaultLatLng, MapOptions.defaultZoom));
            Log.w(TAG, "Centering map on default " + MapOptions.defaultLatLng);
        }

        // Add map layers
        addLayer(new PlacesLayer(getLayoutInflater()));
        addLayer(new HomeLayer());
        addLayer(new LandingLayer());
        addLayer(new MyPositionLayer());
        updateLayers();
    }

}
