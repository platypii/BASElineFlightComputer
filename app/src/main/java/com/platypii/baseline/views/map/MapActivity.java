package com.platypii.baseline.views.map;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.location.LandingZone;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.measurements.MAltitude;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.views.BaseActivity;
import com.platypii.baseline.views.altimeter.AnalogAltimeterSettable;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MapActivity extends BaseActivity implements MyLocationListener, OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener, GoogleMap.OnCameraMoveListener {
    private static final String TAG = "Map";

    private AnalogAltimeterSettable analogAltimeter;
    private ImageButton homeButton;
    private ImageView crosshair;

    private TouchableMapFragment mapFragment;
    private GoogleMap map; // Might be null if Google Play services APK is not available

    // Layers
    private final List<MapLayer> layers = new ArrayList<>();
    // Used to limit number of layer updates
    private long lastLayerUpdate = 0;
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
        setContentView(R.layout.activity_map);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        analogAltimeter = findViewById(R.id.analogAltimeter);
        homeButton = findViewById(R.id.homeButton);
        crosshair = findViewById(R.id.crosshair);

        analogAltimeter.setOverlay(true);
        analogAltimeter.setAlti(Services.alti);

        // Home button listener
        final ImageButton homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(homeButtonListener);

        // Initialize map
        mapFragment = (TouchableMapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * This should only be called once and when we are sure that {@link #map} is not null.
     */
    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.map = map;
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
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
        addLayers();
        updateLayers();

        // Drag listener
        map.setOnCameraMoveStartedListener(this);
        map.setOnCameraMoveListener(this);
        map.setOnCameraIdleListener(this);

        ready = true;
        Log.w(TAG, "Map ready");
    }

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
        updateLayers();
    }

    private void addLayers() {
        layers.add(new PlacesLayer(map));
        layers.add(new HomeLayer(map));
        layers.add(new LandingLayer(map));
        layers.add(new MyPositionLayer(map));
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if (reason == REASON_GESTURE) {
            dragged = true;
            crosshair.setVisibility(View.VISIBLE);
            homeButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCameraMove() {
        updateLayers();
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

    private void updateLayers() {
        // Only update layers once per second
        if (System.currentTimeMillis() - lastLayerUpdate > maxLayerUpdateDuration) {
            lastLayerUpdate = System.currentTimeMillis();
            for (MapLayer layer : layers) {
                layer.update();
            }
        }
    }

    private void updateLocation() {
        if (ready) {
            final LatLng currentLoc = Services.location.lastLoc.latLng();

            // Update markers and overlays
            updateLayers();

            // Center map on user's location
            if (dragged && lastDrag > 0 && System.currentTimeMillis() - lastDrag > MapOptions.SNAP_BACK_TIME) {
                Log.i(TAG, "Snapping back to current location");
                // Snap back to point
                dragged = false;
                lastDrag = 0;
                // Hide crosshair
                crosshair.setVisibility(View.GONE);
                homeButton.setVisibility(View.GONE);
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

    /**
     * Listen for altitude updates
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAltitudeEvent(MAltitude alt) {
        updateAltimeter();
    }

    private void updateAltimeter() {
        analogAltimeter.setAltitude(Services.alti.altitudeAGL());
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start sensor updates
        Services.location.addListener(this);
        EventBus.getDefault().register(this);
        // Recenter on last location
        if (Services.location.lastLoc != null) {
            updateLocation();
        }
        updateAltimeter();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop sensor updates
        Services.location.removeListener(this);
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapFragment.removeOnTouchListeners();
    }

}
