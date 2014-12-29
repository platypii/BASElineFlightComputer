package com.platypii.baseline.ui;

import java.util.ArrayList;
import java.util.List;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.platypii.baseline.R;
import com.platypii.baseline.audible.MyFlightManager;
import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyLocation;
import com.platypii.baseline.data.MyLocationListener;
import com.platypii.baseline.data.MyLocationManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class AccuracyFragment extends MapFragment implements MyLocationListener, OnCameraChangeListener {

    private GoogleMap map;

    private boolean dragged = false;
    private long lastDrag = 0;
    private static final long SNAP_BACK_TIME = 5000; // millis
    private static final int DURATION = 800; // camera animation duration millis

    private boolean paused = false;

    private Marker myMarker;
    private Marker myLanding;
    private Polyline targetBearing;
    private final List<LatLng> pointList = new ArrayList<>();

    public AccuracyFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        map = getMap();

        map.setMyLocationEnabled(true);
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        final LatLng home = new LatLng(38.584191, -121.851634);
        final float zoom = getZoom();
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(home, zoom));

        // Add markers
        myLanding = map.addMarker(new MarkerOptions()
            .position(home)
            .visible(false)
            .title("landing")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_crosshair))
            .anchor(0.5f,0.5f)
        );
        myMarker = map.addMarker(new MarkerOptions()
            .position(home)
            .visible(false)
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_parachute))
            .anchor(0.5f,1f)
        );
        targetBearing = map.addPolyline(new PolylineOptions()
            .visible(false)
            .width(4)
            .color(0x55ffffff)
        );

        // Start GPS updates
        MyLocationManager.addListener(this);

        // Drag listener
        map.setOnCameraChangeListener(this);

        return view;
    }

    public void onLocationChanged(MyLocation loc) {
        if(!paused) {
            final LatLng currentLatLng = loc.latLng();

            // Update current location marker
            myMarker.setPosition(currentLatLng);
            myMarker.setVisible(true);

            // Update accuracy trick marker
            final Location landingLocation = MyFlightManager.getLandingLocation();
            if(landingLocation != null) {
                final LatLng landingLatLng = new LatLng(landingLocation.getLatitude(), landingLocation.getLongitude());
                myLanding.setPosition(new LatLng(landingLocation.getLatitude(), landingLocation.getLongitude()));
                myLanding.setVisible(true);
                pointList.clear();
                pointList.add(currentLatLng);
                pointList.add(landingLatLng);
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
        if(MyAltimeter.altitude < 100) {
            return 18;
        } else if(MyAltimeter.altitude < 600) {
            // Smooth scaling from 100m:18 -> 600m:12
            double alt_a = 100;
            double alt_b = 600;
            float zoom_a = 18;
            float zoom_b = 12;
            return zoom_b + (float) ((alt_b - MyAltimeter.altitude) * (zoom_a - zoom_b) / (alt_b - alt_a));
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

    public void onCameraChange(CameraPosition position) {
        dragged = true;
        lastDrag = System.currentTimeMillis();
    }

}
