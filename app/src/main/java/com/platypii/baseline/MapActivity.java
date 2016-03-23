package com.platypii.baseline;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyFlightManager;
import com.platypii.baseline.data.MyLocationListener;
import com.platypii.baseline.data.MyLocationManager;
import com.platypii.baseline.data.measurements.MLocation;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends FragmentActivity implements MyLocationListener, GoogleMap.OnCameraChangeListener, OnMapReadyCallback {
    private static final String TAG = "Map";

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 64;

    private GoogleMap map; // Might be null if Google Play services APK is not available

    // Markers
    private Marker myLanding;
    private Polyline targetBearing;
    private final List<LatLng> pointList = new ArrayList<>();

    // Activity state
    private boolean paused = false;
    private static boolean firstLoad = true;
    private boolean ready = false;

    // Drag listener
    private boolean dragged = false;
    private long lastDrag = 0;
    private static final long SNAP_BACK_TIME = 5000; // millis
    private static final int DURATION = 800; // camera animation duration millis

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_map);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Initialize map
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Start GPS updates
        MyLocationManager.addListener(this);
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera.
     * This should only be called once and when we are sure that {@link #map} is not null.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        this.map = map;
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Enable location on map
            try {
                map.setMyLocationEnabled(true);
            } catch(SecurityException e) {
                Log.e(TAG, "Error enabling location", e);
            }
        } else {
            // request the missing permissions
            final String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSIONS_REQUEST_LOCATION);
        }
        if (firstLoad) {
            final LatLng usa = new LatLng(41.2, -120.5);
            Log.w(TAG, "Centering map on default view " + usa);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(usa, 5));
            firstLoad = false;
        }

        // Add ui elements
        addMarkers();

        // Drag listener
        map.setOnCameraChangeListener(this);

        ready = true;
        Log.w(TAG, "Map ready");
    }

    private void addMarkers() {
        final LatLng home = new LatLng(47.239, -123.143);

        // Add markers
        myLanding = map.addMarker(new MarkerOptions()
                        .position(home)
                        .visible(false)
                        .title("landing")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_crosshair))
                        .anchor(0.5f,0.5f)
        );
        targetBearing = map.addPolyline(new PolylineOptions()
                        .visible(false)
                        .width(4)
                        .color(0x55ffffff)
        );
    }

    @Override
    public void onCameraChange(CameraPosition position) {
        dragged = true;
        lastDrag = System.currentTimeMillis();
    }

    @Override
    public void onLocationChanged(MLocation loc) {}
    @Override
    public void onLocationChangedPostExecute() {
        if(ready && !paused) {
            final MLocation loc = MyLocationManager.lastLoc;
            final LatLng currentLatLng = loc.latLng();

            // Update accuracy trick marker
            final LatLng landingLocation = MyFlightManager.getLandingLocation();
            if(landingLocation != null) {
                myLanding.setPosition(landingLocation);
                myLanding.setVisible(true);
                pointList.clear();
                pointList.add(currentLatLng);
                pointList.add(landingLocation);
                targetBearing.setPoints(pointList);
                targetBearing.setVisible(true);
            } else {
                myLanding.setVisible(false);
                targetBearing.setVisible(false);
            }

            // Center map on user's location
            if(dragged && System.currentTimeMillis() - lastDrag > SNAP_BACK_TIME) {
                // Snap back to point
                dragged = false;
                // Zoom based on altitude
                final float zoom = getZoom();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, zoom), DURATION, null);
            } else if(!dragged) {
                // TODO: Jump to point
                // map.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                // Zoom based on altitude
                final float zoom = getZoom();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, zoom), DURATION, null);
            }
        }
    }

    /**
     * Returns the default zoom for a given altitude
     */
    private static float getZoom() {
        final double altitude = MyAltimeter.altitudeAGL();
        if(altitude < 100) {
            return 18;
        } else if(altitude < 600) {
            // Smooth scaling from 100m:18 -> 600m:12
            double alt_a = 100;
            double alt_b = 600;
            float zoom_a = 18;
            float zoom_b = 12;
            return zoom_b + (float) ((alt_b - altitude) * (zoom_a - zoom_b) / (alt_b - alt_a));
        } else {
            return 12;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        paused = false;
        if(MyLocationManager.lastLoc != null) {
            onLocationChanged(MyLocationManager.lastLoc);
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        paused = true;
    }
}
