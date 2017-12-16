package com.platypii.baseline.views.map;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.views.altimeter.AnalogAltimeterSettable;
import com.platypii.baseline.location.LandingZone;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.measurements.MAltitude;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Numbers;
import com.platypii.baseline.views.BaseActivity;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends BaseActivity implements MyLocationListener, OnMapReadyCallback, GoogleMap.OnCameraMoveStartedListener, GoogleMap.OnCameraIdleListener {
    private static final String TAG = "Map";

    private AnalogAltimeterSettable analogAltimeter;
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
    private Marker myPositionMarker;
    private BitmapDescriptor myposition1;
    private BitmapDescriptor myposition2;

    // Activity state
    private boolean ready = false;

    // Drag listener
    private boolean dragged = false;
    private long lastDrag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_map);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        analogAltimeter = findViewById(R.id.analogAltimeter);
        flightStatsVario = findViewById(R.id.flightStatsVario);
        flightStatsSpeed = findViewById(R.id.flightStatsSpeed);
        flightStatsGlide = findViewById(R.id.flightStatsGlide);
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
        if(Services.location.lastLoc != null) {
            final LatLng center = Services.location.lastLoc.latLng();
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(center, MapOptions.getZoom()));
            Log.i(TAG, "Centering map on " + center);
        } else if(LandingZone.homeLoc != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(LandingZone.homeLoc, MapOptions.defaultZoom));
            Log.w(TAG, "Centering map on home " + LandingZone.homeLoc);
        } else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(MapOptions.defaultLatLng, MapOptions.defaultZoom));
            Log.w(TAG, "Centering map on default " + MapOptions.defaultLatLng);
        }

        myposition1 = BitmapDescriptorFactory.fromResource(R.drawable.myposition1);
        myposition2 = BitmapDescriptorFactory.fromResource(R.drawable.myposition2);

        // Add ui elements
        addMarkers();
        updateHome();
        updateMyPosition();

        // Drag listener
        map.setOnCameraMoveStartedListener(this);
        map.setOnCameraIdleListener(this);

        ready = true;
        Log.w(TAG, "Map ready");
    }

    private final View.OnClickListener homeButtonListener = new View.OnClickListener() {
        public void onClick(View arg0) {
            if(map != null) {
                final LatLng center = map.getCameraPosition().target;
                if(center.equals(LandingZone.homeLoc)) {
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
        LandingZone.homeLoc = home;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MapActivity.this);
        final SharedPreferences.Editor editor = prefs.edit();
        if(home != null) {
            editor.putString("home_latitude", Double.toString(home.latitude));
            editor.putString("home_longitude", Double.toString(home.longitude));
        } else {
            editor.putString("home_latitude", null);
            editor.putString("home_longitude", null);
        }
        editor.apply();
        updateHome();
    }

    private void addMarkers() {
        final LatLng home = new LatLng(47.239, -123.143); // Kpow

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
                        .startCap(new RoundCap())
                        .endCap(new RoundCap())
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
                        .startCap(new RoundCap())
                        .endCap(new RoundCap())
        );
        myPositionMarker = map.addMarker(new MarkerOptions()
                .position(home)
                .visible(false)
                .icon(myposition2)
                .anchor(0.5f, 0.5f)
                .flat(true)
        );
    }

    @Override
    public void onCameraMoveStarted(int reason) {
        if(reason == REASON_GESTURE) {
            dragged = true;
            crosshair.setVisibility(View.VISIBLE);
            homeButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCameraIdle() {
        if(dragged) {
            Log.d(TAG, "Camera idle after drag");
            lastDrag = System.currentTimeMillis();
        }
    }

    @Override
    public void onLocationChanged(@NonNull MLocation loc) {}
    @Override
    public void onLocationChangedPostExecute() {
        updateFlightStats();
        if(ready) {
            final LatLng currentLoc = Services.location.lastLoc.latLng();

            // Update markers and overlays
            updateMyPosition();
            updateHome();
            updateLanding();

            // Center map on user's location
            if(dragged && lastDrag > 0 && System.currentTimeMillis() - lastDrag > MapOptions.SNAP_BACK_TIME) {
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
            } else if(!dragged) {
                // Alternate behavior: jump to point
                // map.animateCamera(CameraUpdateFactory.newLatLng(latLng));

                // Zoom based on altitude
                final float zoom = MapOptions.getZoom();
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLoc, zoom), MapOptions.zoomDuration(), null);
            }
        }
    }

    /**
     * Listen for altitude updates
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAltitudeEvent(MAltitude alt) {
        updateFlightStats();
    }

    private void updateHome() {
        if(LandingZone.homeLoc != null) {
            homeMarker.setPosition(LandingZone.homeLoc);
            homeMarker.setVisible(true);
            if(Services.location.lastLoc != null) {
                final LatLng currentLoc = Services.location.lastLoc.latLng();
                homePoints.clear();
                homePoints.add(currentLoc);
                homePoints.add(LandingZone.homeLoc);
                homePath.setPoints(homePoints);
                homePath.setVisible(true);
            }
        } else {
            homeMarker.setVisible(false);
            homePath.setVisible(false);
        }
    }

    private void updateLanding() {
        final LatLng landingLocation = LandingZone.getLandingLocation();
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
        analogAltimeter.setAltitude(Services.alti.altitudeAGL());
        if(Services.alti.climb < 0) {
            flightStatsVario.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_downward_white_24dp,0,0,0);
            flightStatsVario.setText(Convert.speed(-Services.alti.climb));
        } else {
            flightStatsVario.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_upward_white_24dp,0,0,0);
            flightStatsVario.setText(Convert.speed(Services.alti.climb));
        }
        final double groundSpeed = Services.location.groundSpeed();
        if(Numbers.isReal(groundSpeed)) {
            flightStatsSpeed.setText(Convert.speed(groundSpeed));
            flightStatsGlide.setText(Convert.glide(groundSpeed, Services.alti.climb, 2, true));
        } else {
            flightStatsSpeed.setText("");
            flightStatsGlide.setText("");
        }
    }

    private void updateMyPosition() {
        if(Services.location.isFresh()) {
            myPositionMarker.setVisible(true);
            myPositionMarker.setPosition(Services.location.lastLoc.latLng());
            final double groundSpeed = Services.location.groundSpeed();
            final double bearing = Services.location.bearing();
            if(Numbers.isReal(bearing) && groundSpeed > 0.1) {
                // Speed > 0.2mph
                myPositionMarker.setIcon(myposition1);
                myPositionMarker.setRotation((float) bearing);
            } else {
                myPositionMarker.setIcon(myposition2);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start sensor updates
        Services.location.addListener(this);
        EventBus.getDefault().register(this);
        // Recenter on last location
        if(Services.location.lastLoc != null) {
            onLocationChangedPostExecute();
        }
        updateFlightStats();
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
