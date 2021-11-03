package com.platypii.baseline.views.map;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.databinding.ActivityMapBinding;
import com.platypii.baseline.location.LandingZone;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.views.BaseActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

        // Home button listener
        binding.homeButton.setOnClickListener(homeButtonListener);

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

        ready = true;
        Log.w(TAG, "Map ready");
    }

    @NonNull
    private final View.OnClickListener homeButtonListener = new View.OnClickListener() {
        public void onClick(View arg0) {
            if (map != null) {
                final LatLng center = map.getCameraPosition().target;
                if (center.equals(LandingZone.homeLoc)) {
                    // Dropped pin on exact same location, delete home
                    setHome(null);
                } else {
                    // Set home location to map center
                    setHome(center);
                }
            }
        }
    };

    private void setHome(@Nullable LatLng home) {
        Log.i(TAG, "Setting home location: " + home);
        LandingZone.setHomeLocation(this, home);
        updateLayers(true);
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == REASON_GESTURE) {
            dragged = true;
            binding.crosshair.setVisibility(View.VISIBLE);
            binding.homeButton.setVisibility(View.VISIBLE);
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
            if (dragged && lastDrag > 0 && System.currentTimeMillis() - lastDrag > MapOptions.snapbackTime()) {
                Log.i(TAG, "Snapping back to current location");
                // Snap back to point
                dragged = false;
                lastDrag = 0;
                // Hide crosshair
                binding.crosshair.setVisibility(View.GONE);
                binding.homeButton.setVisibility(View.GONE);
                // Zoom based on altitude
                final float zoom = MapOptions.getZoom();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, zoom), MapOptions.zoomDuration(), null);
            } else if (!dragged) {
                // Alternate behavior: jump to point
                // map.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                // Zoom based on altitude
                final float zoom = MapOptions.getZoom();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, zoom), MapOptions.zoomDuration(), null);
            }
        }
    }

    private final Runnable updateLocationRunnable = this::updateLocation;

    @Override
    protected void onResume() {
        super.onResume();
        // Start location updates
        Services.location.addListener(this);
        // Recenter on last location
        if (Services.location.lastLoc != null) {
            updateLocation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop location updates
        Services.location.removeListener(this);
    }

}
