package com.platypii.baseline.views.map;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.databinding.ActivityMapBinding;
import com.platypii.baseline.location.LandingZone;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.BaseActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

public class MapActivity extends BaseActivity implements MyLocationListener, OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveListener {
    private static final String TAG = "Map";

    private ActivityMapBinding binding;
    @Nullable
    private MapFragment mapFragment;
    @Nullable
    private GoogleMap map; // Might be null if Google Play services APK is not available

    // Limit the number of layer updates
    private long lastLayerUpdate = 0; // timestamp
    private static final long maxLayerUpdateDuration = 500; // don't update layers more than once every 500ms

    // Activity state
    private boolean ready = false;

    // Drag listener
    private boolean dragged = false;
    private long lastDrag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON | WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = ActivityMapBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize map
        mapFragment = (MapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Failed to get map fragment");
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * This should only be called once and when we are sure that {@link #map} is not null.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.map = map;

        // Drag listener
        map.setOnCameraMoveStartedListener(this);
        map.setOnCameraMoveListener(this);
        map.setOnCameraIdleListener(this);

        try {
            if (MapState.mapBounds != null) {
                // Restore map bounds
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(MapState.mapBounds, 0));
                Log.i(TAG, "Centering map on " + MapState.mapBounds);
            } else if (Services.location.lastLoc != null) {
                // Center on last location
                final LatLng center = Services.location.lastLoc.latLng();
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, MapOptions.getZoom()));
                Log.i(TAG, "Centering map on " + center);
            } else {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(MapOptions.defaultLatLng, MapOptions.defaultZoom));
                Log.w(TAG, "Centering map on default " + MapOptions.defaultLatLng);
            }
        } catch (Exception e) {
            Exceptions.report(e);
        }

        ready = true;
        Log.w(TAG, "Map ready");
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == REASON_GESTURE) {
            dragged = true;
        }
    }

    @Override
    public void onCameraMove() {
        updateLayers(false);
    }

    @Override
    public void onCameraIdle() {
        if (dragged) {
            Log.d(TAG, "Camera idle after drag");
            lastDrag = System.currentTimeMillis();
        }
    }

    @Override
    public void onLocationChanged(@NonNull MLocation loc) {
        runOnUiThread(updateLocationRunnable);
    }

    void setHome(@Nullable LatLng home) {
        Log.i(TAG, "Setting home location: " + home);
        LandingZone.setHomeLocation(this, home);
        updateLayers(true);
    }

    @Nullable
    LatLng getCenter() {
        if (map != null) {
            return map.getCameraPosition().target;
        } else {
            return null;
        }
    }

    void setCenter(@NonNull LatLng latLng, float zoom) {
        if (map != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        }
    }

    void updatePlacesLayer() {
        if (mapFragment != null) {
            mapFragment.placesLayer.update();
        }
    }

    void resetLastDrag() {
        lastDrag = 1; // Needs to be greater than 0
    }

    private void updateLayers(boolean force) {
        // Only update layers once per second
        if (mapFragment != null && (force || System.currentTimeMillis() - lastLayerUpdate > maxLayerUpdateDuration)) {
            lastLayerUpdate = System.currentTimeMillis();
            mapFragment.updateLayers();
        }
    }

    private void updateLocation() {
        if (ready && map != null) {
            final LatLng currentLoc = Services.location.lastLoc.latLng();

            // Update markers and overlays
            updateLayers(false);

            // Center map on user's location
            if (!MapState.menuOpen) {
                if (dragged && lastDrag > 0 && System.currentTimeMillis() - lastDrag > MapOptions.snapbackTime()) {
                    Log.i(TAG, "Snapping back to current location");
                    // Snap back to point
                    dragged = false;
                    lastDrag = 0;
                    final float zoom = MapOptions.getZoom(); // Zoom based on altitude
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, zoom), MapOptions.zoomDuration(), null);
                } else if (!dragged) {
                    // Alternate behavior: jump to point
                    // map.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                    final float zoom = MapOptions.getZoom(); // Zoom based on altitude
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, zoom), MapOptions.zoomDuration(), null);
                }
            }
        }
    }

    private final Runnable updateLocationRunnable = this::updateLocation;

    @Override
    protected void onResume() {
        super.onResume();
        // Start location updates
        Services.location.addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates
        Services.location.removeListener(this);
        // Save map bounds
        if (map != null) {
            MapState.mapBounds = map.getProjection().getVisibleRegion().latLngBounds;
        }
    }

}
