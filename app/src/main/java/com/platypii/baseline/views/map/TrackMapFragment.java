package com.platypii.baseline.views.map;

import com.platypii.baseline.R;
import com.platypii.baseline.events.ChartFocusEvent;
import com.platypii.baseline.location.Geo;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.views.charts.layers.Colors;
import com.platypii.baseline.views.tracks.TrackDataActivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TrackMapFragment extends SupportMapFragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener {
    private static final String TAG = "TrackMapFrag";

    private List<MLocation> trackData;

    @Nullable
    private GoogleMap map;
    @Nullable
    private Marker focusMarker;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.map = map;
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        // Get track data from parent activity
        final Activity parent = getActivity();
        if (parent instanceof TrackDataActivity) {
            ((TrackDataActivity) parent).trackData.thenAccept(trackData -> {
                loadTrack(map, trackData);
            });
        }
        // Listen for touch
        map.setOnMapClickListener(this);
    }

    private void loadTrack(@NonNull GoogleMap map, @NonNull TrackData trackData) {
        this.trackData = trackData.data;
        final PolylineOptions plane = new PolylineOptions().color(Colors.modePlane);
        final PolylineOptions flight = new PolylineOptions().color(Colors.modeWingsuit);
        final PolylineOptions canopy = new PolylineOptions().color(Colors.modeCanopy);
        final PolylineOptions ground = new PolylineOptions().color(Colors.modeGround);
        for (MLocation loc : trackData.data) {
            if (loc.millis <= trackData.stats.exit.millis) {
                plane.add(loc.latLng());
            }
            if (trackData.stats.exit.millis <= loc.millis && loc.millis <= trackData.stats.deploy.millis) {
                flight.add(loc.latLng());
            }
            if (trackData.stats.deploy.millis <= loc.millis && loc.millis <= trackData.stats.land.millis) {
                canopy.add(loc.latLng());
            }
            if (trackData.stats.land.millis <= loc.millis) {
                ground.add(loc.latLng());
            }
        }
        map.addPolyline(ground);
        map.addPolyline(canopy);
        map.addPolyline(flight);
        map.addPolyline(plane);
        if (trackData.stats.bounds != null) {
            final float density = getResources().getDisplayMetrics().density;
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(trackData.stats.bounds, (int) (20 * density)));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChartFocus(@NonNull ChartFocusEvent event) {
        final MLocation focus = event.location;
        if (focus != null && map != null) {
            Log.i(TAG, "Focusing track map on " + focus);
            if (focusMarker == null) {
                focusMarker = map.addMarker(new MarkerOptions()
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin))
                        .anchor(0.5f, 1.0f)
                        .position(focus.latLng())
                );
            } else {
                focusMarker.setPosition(focus.latLng());
                focusMarker.setVisible(true);
            }
        } else {
            if (focusMarker != null) {
                focusMarker.setVisible(false);
            }
        }
    }

    @Override
    public void onMapClick(LatLng focus) {
        // Find closest point
        final MLocation closest = findClosest(focus);
        EventBus.getDefault().post(new ChartFocusEvent(closest));
    }

    /**
     * Performs a search for the nearest data point
     */
    @Nullable
    private MLocation findClosest(@Nullable LatLng focus) {
        MLocation closest = null;
        if (focus != null && trackData != null && !trackData.isEmpty()) {
            double closestDistance = Double.POSITIVE_INFINITY;
            for (MLocation loc : trackData) {
                final double distance = Geo.fastDistance(loc.latitude, loc.longitude, focus.latitude, focus.longitude);
                if (distance < closestDistance) {
                    closest = loc;
                    closestDistance = distance;
                }
            }
        }
        return closest;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

}
