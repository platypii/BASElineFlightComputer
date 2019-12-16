package com.platypii.baseline.views.map.layers;

import androidx.annotation.NonNull;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

public abstract class MapLayer {

    public abstract void onAdd(@NonNull GoogleMap map);

    public void update() {
    }

    public void onMapClick(LatLng latLng) {
    }

    public void onRemove() {
    }

}
