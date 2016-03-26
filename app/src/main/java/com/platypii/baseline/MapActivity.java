package com.platypii.baseline;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import com.platypii.baseline.data.Convert;
import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyAltitudeListener;
import com.platypii.baseline.data.MyFlightManager;
import com.platypii.baseline.data.MyLocationListener;
import com.platypii.baseline.data.MyLocationManager;
import com.platypii.baseline.data.measurements.MAltitude;
import com.platypii.baseline.data.measurements.MLocation;
import com.platypii.baseline.util.Util;

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

        analogAltimeter = (AnalogAltimeter) findViewById(R.id.analogAltimeter);
        flightStatsAltimeter = (TextView) findViewById(R.id.flightStatsAltimeter);
        flightStatsVario = (TextView) findViewById(R.id.flightStatsVario);
        flightStatsSpeed = (TextView) findViewById(R.id.flightStatsSpeed);
        flightStatsGlide = (TextView) findViewById(R.id.flightStatsGlide);

        analogAltimeter.setLongClickable(true);
        analogAltimeter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                promptForAltitude();
                return false;
            }
        });

        // Home button listener
        final ImageButton homeButton = (ImageButton) findViewById(R.id.homeButton);
        homeButton.setOnClickListener(homeButtonListener);

        // Initialize map
        final SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Start sensor updates
        MyLocationManager.addListener(this);
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
            final LatLng kpow = new LatLng(47.239, -123.143);
            // final LatLng usa = new LatLng(41.2, -120.5);
            Log.w(TAG, "Centering map on default view " + kpow);
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(kpow, 5));
            firstLoad = false;
        }

        // Add ui elements
        addMarkers();

        // Drag listener
        map.setOnCameraChangeListener(this);

        ready = true;
        Log.w(TAG, "Map ready");
    }

    private View.OnClickListener homeButtonListener = new View.OnClickListener() {
        public void onClick(View arg0) {
            if(map != null) {
                // Set home location to map center
                final LatLng loc = map.getCameraPosition().target;
                MyFlightManager.homeLoc = loc;
                Log.i(TAG, "Setting home location: " + loc);

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
                        .anchor(0.5f,1.0f)
        );
        // Add line to home
        homePath = map.addPolyline(new PolylineOptions()
                        .visible(false)
                        .width(8)
                        .color(0x66ffffff)
        );
        // Add projected landing zone
        landingMarker = map.addMarker(new MarkerOptions()
                        .position(home)
                        .visible(false)
                        .title("landing")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_crosshair))
                        .anchor(0.5f,0.5f)
        );
        // Add line to projected landing zone
        landingPath = map.addPolyline(new PolylineOptions()
                        .visible(false)
                        .width(8)
                        .color(0x66ff0000)
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
        if(!paused) {
            updateFlightStats();
        }
        if(ready && !paused) {
            final LatLng currentLoc = MyLocationManager.lastLoc.latLng();

            // Update home path
            updateHome();

            // Update accuracy trick marker
            updateLanding();

            // Center map on user's location
            if(dragged && System.currentTimeMillis() - lastDrag > SNAP_BACK_TIME) {
                // Snap back to point
                dragged = false;
                // Zoom based on altitude
                final float zoom = getZoom();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, zoom), DURATION, null);
            } else if(!dragged) {
                // Alternate behavior: jump to point
                // map.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                // Zoom based on altitude
                final float zoom = getZoom();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, zoom), DURATION, null);
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
            if(MyLocationManager.lastLoc != null) {
                final LatLng currentLoc = MyLocationManager.lastLoc.latLng();
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
            final LatLng currentLoc = MyLocationManager.lastLoc.latLng();
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

    /**
     * Returns the default zoom for a given altitude
     */
    private static float getZoom() {
        final double altitude = MyAltimeter.altitudeAGL();

        // Piecewise linear zoom function
        final double alts[] = {100, 600, 2000};
        final float zooms[] = {17.9f, 14f, 12.5f};

        if(altitude < alts[0]) {
            return zooms[0];
        } else if(altitude <= alts[1]) {
            // Linear interpolation
            return zooms[1] - (float) ((alts[1] - altitude) * (zooms[1] - zooms[0]) / (alts[1] - alts[0]));
        } else if(altitude <= alts[2]) {
            // Linear interpolation
            return zooms[2] - (float) ((alts[2] - altitude) * (zooms[2] - zooms[1]) / (alts[2] - alts[1]));
        } else {
            return zooms[2];
        }
    }

    private void updateFlightStats() {
        analogAltimeter.setAltitude(MyAltimeter.altitudeAGL());
        final MLocation loc = MyLocationManager.lastLoc;
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

    private void promptForAltitude() {
        Log.i(TAG, "Prompting for ground level adjustment");
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Set Altitude AGL");
        builder.setMessage("Altitude above ground level in feet");
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("0");
        builder.setView(input);
        builder.setPositiveButton(R.string.set_altitude, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                final String inputText = input.getText().toString();
                final double altitude = inputText.isEmpty()? 0.0 : Util.parseDouble(inputText) * Convert.FT;
                if(Util.isReal(altitude)) {
                    Log.w(TAG, "Setting altitude above ground level to " + altitude + "m");
                    MyAltimeter.ground_level = MyAltimeter.altitude - altitude;
                } else {
                    Log.e(TAG, "Invalid altitude above ground level: " + altitude);
                    Toast.makeText(MapActivity.this, "Invalid altitude", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        // Create the AlertDialog
        final AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        paused = false;
        if(MyLocationManager.lastLoc != null) {
            onLocationChangedPostExecute();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        paused = true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Stop sensor updates
        MyLocationManager.removeListener(this);
        MyAltimeter.removeListener(this);
    }
}
