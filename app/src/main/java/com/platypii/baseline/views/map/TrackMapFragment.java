package com.platypii.baseline.views.map;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.PolylineOptions;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.views.charts.ChartsFragment;
import com.platypii.baseline.views.charts.layers.Colors;
import com.platypii.baseline.views.tracks.TrackRemoteActivity;

public class TrackMapFragment extends SupportMapFragment implements OnMapReadyCallback {

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        // Get track data from parent activity
        final Activity parent = getActivity();
        if (parent instanceof TrackRemoteActivity) {
            ((TrackRemoteActivity) parent).trackData.thenAccept(trackData -> {
                loadTrack(map, trackData);
            });
        }
    }

    private void loadTrack(@NonNull GoogleMap map, @NonNull TrackData trackData) {
        final PolylineOptions polyline = new PolylineOptions().color(Colors.defaultColor);
        for (MLocation point : trackData.data) {
            polyline.add(point.latLng());
        }
        map.addPolyline(polyline);
        if (trackData.stats.bounds != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(trackData.stats.bounds, 2));
        }
    }

}
