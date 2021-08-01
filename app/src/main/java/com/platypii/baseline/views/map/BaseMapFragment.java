package com.platypii.baseline.views.map;

import com.platypii.baseline.views.map.layers.MapLayer;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;

/**
 * Implements a layered map
 */
public class BaseMapFragment extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnCameraMoveListener, GoogleMap.OnMapClickListener {

    // Null if Google Play services APK is not available
    @Nullable
    private GoogleMap map;

    private final List<MapLayer> layers = new ArrayList<>();

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.map = map;
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        // Listen for touch
        map.setOnMapClickListener(this);
        // Notify layers that map is ready
        for (MapLayer layer : layers) {
            layer.onAdd(map);
        }
    }

    @Override
    public void onCameraMove() {
        updateLayers();
    }

    @Override
    public void onMapClick(@NonNull LatLng latLng) {
        for (MapLayer layer : layers) {
            layer.onMapClick(latLng);
        }
    }

    void addLayer(@NonNull MapLayer layer) {
        layers.add(layer);
        if (map != null) {
            layer.onAdd(map);
        }
    }

    void removeLayer(@NonNull MapLayer layer) {
        layers.remove(layer);
        layer.onRemove();
    }

    void updateLayers() {
        for (MapLayer layer : layers) {
            layer.update();
        }
    }

}
