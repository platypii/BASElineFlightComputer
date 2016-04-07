package com.platypii.baseline;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.InflateException;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.platypii.baseline.data.Convert;
import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyAltitudeListener;
import com.platypii.baseline.data.MyFlightManager;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.data.measurements.MAltitude;
import com.platypii.baseline.data.measurements.MLocation;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends FragmentActivity implements MyLocationListener, MyAltitudeListener, GoogleMap.OnCameraChangeListener, OnMapReadyCallback {
    private static final String TAG = "Map";

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 64;

    private AnalogAltimeter analogAltimeter;
    private TextView flightStatsAltimeter;
    private TextView flightStatsVario;
    private TextView flightStatsSpeed;
    private TextView flightStatsGlide;
    private ImageButton homeButton;
    private ImageView crosshair;

    private TouchableMapFragment mapFragment;
    private GoogleMap map; // Might be null if Google Play services APK is not available

    // Markers
    private Marker homeMarker;
    private Polyline homePath;
    private final List<LatLng> homePoints = new ArrayList<>();
    private Marker landingMarker;
    private Polyline landingPath;
    private final List<LatLng> landingPoints = new ArrayList<>();

    // Activity state
    private boolean paused = false;
    private static boolean firstLoad = true;
    private boolean ready = false;

    // Drag listener
    private boolean dragged = false;
    private long lastDrag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        try {
            setContentView(R.layout.activity_map);
        } catch(InflateException e) {
            Log.e(TAG, "Google maps error #9021", e);
            Toast.makeText(this, "Google maps error #9021", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        analogAltimeter = (AnalogAltimeter) findViewById(R.id.analogAltimeter);
        flightStatsAltimeter = (TextView) findViewById(R.id.flightStatsAltimeter);
        flightStatsVario = (TextView) findViewById(R.id.flightStatsVario);
        flightStatsSpeed = (TextView) findViewById(R.id.flightStatsSpeed);
        flightStatsGlide = (TextView) findViewById(R.id.flightStatsGlide);
        homeButton = (ImageButton) findViewById(R.id.homeButton);
        crosshair = (ImageView) findViewById(R.id.crosshair);

        analogAltimeter.setOverlay(true);
        analogAltimeter.setLongClickable(true);
        analogAltimeter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                AltimeterActivity.promptForAltitude(MapActivity.this);
                return false;
            }
        });

        // Home button listener
        final ImageButton homeButton = (ImageButton) findViewById(R.id.homeButton);
        homeButton.setOnClickListener(homeButtonListener);

        // Initialize map
        mapFragment = (TouchableMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapFragment.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        lastDrag = System.currentTimeMillis();
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Start flight services
        Services.start(this);
        // Start sensor updates
        Services.location.addListener(this);
        MyAltimeter.addListener(this);
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
            Log.w(TAG, "Centering map on default view " + MapOptions.defaultLatLng);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(MapOptions.defaultLatLng, MapOptions.defaultZoom));
            firstLoad = false;
        }

        // Add ui elements
        addMarkers();
        updateHome();

        // Drag listener
        map.setOnCameraChangeListener(this);

        ready = true;
        Log.w(TAG, "Map ready");
    }

    private final View.OnClickListener homeButtonListener = new View.OnClickListener() {
        public void onClick(View arg0) {
            if(map != null) {
                // Set home location to map center
                final LatLng center = map.getCameraPosition().target;
                Log.i(TAG, "Setting home location: " + center);
                MyFlightManager.homeLoc = center;
                final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MapActivity.this);
                final SharedPreferences.Editor editor = prefs.edit();
                editor.putString("home_latitude", Double.toString(center.latitude));
                editor.putString("home_longitude", Double.toString(center.longitude));
                editor.apply();

                // Update map overlay
                updateHome();
            }
        }
    };

    private void addMarkers() {
        final LatLng home = new LatLng(47.239, -123.143);

        // Add home location pin
        homeMarker = map.addMarker(new MarkerOptions()
                        .position(home)
                        .visible(false)
                        .title("home")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin))
                        .anchor(0.5f, 1.0f)
        );
        // Add line to home
        homePath = map.addPolyline(new PolylineOptions()
                        .visible(false)
                        .width(10)
                        .color(0x66ffffff)
        );
        // Add projected landing zone
        landingMarker = map.addMarker(new MarkerOptions()
                        .position(home)
                        .visible(false)
                        .title("landing")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_target))
                        .anchor(0.5f, 0.5f)
        );
        // Add line to projected landing zone
        landingPath = map.addPolyline(new PolylineOptions()
                        .visible(false)
                        .width(10)
                        .color(0x66ff0000)
        );
    }

    @Override
    public void onCameraChange(CameraPosition position) {
        if(System.currentTimeMillis() - lastDrag < MapOptions.SNAP_BACK_TIME) {
            dragged = true;
            crosshair.setVisibility(View.VISIBLE);
            homeButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLocationChanged(MLocation loc) {}
    @Override
    public void onLocationChangedPostExecute() {
        if(!paused) {
            updateFlightStats();
        }
        if(ready && !paused) {
            final LatLng currentLoc = Services.location.lastLoc.latLng();

            // Update home path
            updateHome();

            // Update accuracy trick marker
            updateLanding();

            // Center map on user's location
            if(dragged && System.currentTimeMillis() - lastDrag > MapOptions.SNAP_BACK_TIME) {
                Log.i(TAG, "Snapping back to current location");
                // Snap back to point
                dragged = false;
                // Hide crosshair
                crosshair.setVisibility(View.GONE);
                homeButton.setVisibility(View.GONE);
                // Zoom based on altitude
                final float zoom = MapOptions.getZoom();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, zoom), MapOptions.zoomDuration(), null);
            } else if(!dragged) {
                // Alternate behavior: jump to point
                // map.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                // Zoom based on altitude
                final float zoom = MapOptions.getZoom();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, zoom), MapOptions.zoomDuration(), null);
            }
        }
    }

    // Altitude updates
    @Override
    public void altitudeDoInBackground(MAltitude alt) {}
    @Override
    public void altitudeOnPostExecute() {
        if(!paused) {
            updateFlightStats();
        }
    }

    private void updateHome() {
        if(MyFlightManager.homeLoc != null) {
            homeMarker.setPosition(MyFlightManager.homeLoc);
            homeMarker.setVisible(true);
            if(Services.location.lastLoc != null) {
                final LatLng currentLoc = Services.location.lastLoc.latLng();
                homePoints.clear();
                homePoints.add(currentLoc);
                homePoints.add(MyFlightManager.homeLoc);
                homePath.setPoints(homePoints);
                homePath.setVisible(true);
            }
        }
    }

    private void updateLanding() {
        final LatLng landingLocation = MyFlightManager.getLandingLocation();
        if(landingLocation != null) {
            final LatLng currentLoc = Services.location.lastLoc.latLng();
            landingMarker.setPosition(landingLocation);
            landingMarker.setVisible(true);
            landingPoints.clear();
            landingPoints.add(currentLoc);
            landingPoints.add(landingLocation);
            landingPath.setPoints(landingPoints);
            landingPath.setVisible(true);
        } else {
            landingMarker.setVisible(false);
            landingPath.setVisible(false);
        }
    }

    private void updateFlightStats() {
        analogAltimeter.setAltitude(MyAltimeter.altitudeAGL());
        final MLocation loc = Services.location.lastLoc;
        flightStatsAltimeter.setText(Convert.distance(MyAltimeter.altitudeAGL()));
        if(MyAltimeter.climb < 0) {
            flightStatsVario.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_downward_white_24dp,0,0,0);
            flightStatsVario.setText(Convert.speed(-MyAltimeter.climb));
        } else {
            flightStatsVario.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_upward_white_24dp,0,0,0);
            flightStatsVario.setText(Convert.speed(MyAltimeter.climb));
        }
        if(loc != null) {
            flightStatsSpeed.setText(Convert.speed(loc.groundSpeed()));
            flightStatsGlide.setText(Convert.glide(loc.glideRatio()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        paused = false;
        if(Services.location.lastLoc != null) {
            onLocationChangedPostExecute();
        }
        altitudeOnPostExecute();
    }
    @Override
    public void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    public void onStop() {
        super.onStop();

        // Stop sensor updates
        Services.location.removeListener(this);
        MyAltimeter.removeListener(this);
        // Stop flight services
        Services.stop();
    }

    @Override
    public void onDestroy() {
        try {
            super.onDestroy();
            mapFragment.removeOnTouchListeners();
        } catch(Exception e) {
            Log.e(TAG, "Exception in onDestroy", e);
        }
    }
}
