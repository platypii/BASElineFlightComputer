package com.platypii.baseline.ui;

import com.platypii.baseline.audible.MyFlightManager;
import com.platypii.baseline.data.MyLocation;
import com.platypii.baseline.data.MyLocationListener;
import com.platypii.baseline.data.MyLocationManager;
import com.platypii.baseline.R;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import java.util.List;


public class MapFragment extends Fragment {

    private Context context;
    private FrameLayout mapContainer;
	private MapView mapView;
    private MapController mapControl;

    private boolean dragged = false;
    private long lastDrag = 0;
    private static final long SNAP_BACK_TIME = 5000; // millis
    
    private boolean paused = false;

    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        context = activity;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.map, container, false);

        // Make UI elements
        // mapContainer = new FrameLayout(context);
        mapContainer = (FrameLayout) view.findViewById(R.id.mapContainer);
        if(mapView == null) {
            mapView = new MapView(context, this.getString(R.string.api_map_key));
            mapView.setClickable(true);
            mapView.setSatellite(true);
            mapControl = mapView.getController();
            mapControl.setCenter(new GeoPoint(33631005, -117296275)); // Elsinore
            mapControl.setZoom(21);
        }
        update();
        mapContainer.addView(mapView);

        // Overlay view
        View overlayView = inflater.inflate(R.layout.mapoverlay, container, false);
        mapContainer.addView(overlayView);
 
        // Home button listener
        ImageButton homeButton = (ImageButton) overlayView.findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				// Set home location
				// Use map center
				GeoPoint gp = mapView.getMapCenter();
				Location loc = new Location("Map");
				loc.setLatitude(1E-6 * gp.getLatitudeE6());
				loc.setLongitude(1E-6 * gp.getLongitudeE6());
				MyFlightManager.homeLoc = loc;
				mapView.postInvalidate();
			}
        });

        // Map Overlays
        List<Overlay> overlays = mapView.getOverlays();
        overlays.clear();
        overlays.add(new MyMapOverlay(context));
        //overlays.add(new GpsOverlay());
        MyLocationOverlay mLocOverlay = new MyLocationOverlay(context, mapView);
        mLocOverlay.enableMyLocation();
        //overlays.add(mLocOverlay);
        // overlays.add(new MapScaleBarOverlay(context));

        // Start GPS updates
        MyLocationManager.addListener(new MyLocationListener() {
            public void onLocationChanged(MyLocation loc) {
                update();
            }
        });

        // Drag listener
        mapView.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View arg0, MotionEvent arg1) {
				dragged = true;
				lastDrag = System.currentTimeMillis();
				return false; // also call map drag handler
			}
        });

        return view;
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mapContainer.removeAllViews();
    }

    private void update() {
        MyLocation loc = MyLocationManager.lastLoc;
        if(loc != null && !paused) {
            // Update Map
            GeoPoint gp = Convert.locToGeoPoint(loc.latitude, loc.longitude);
        	if(dragged && System.currentTimeMillis() - lastDrag > SNAP_BACK_TIME) {
        		// Snap back to point
        		dragged = false;
        		mapControl.animateTo(gp);
        	} else if(!dragged) {
        		// Jump to point
	            mapControl.setCenter(gp);
	            
	            // TODO: Zoom depends on speed. Display 10 seconds ahead
	            // double speed = MyLocationManager.groundSpeed;
	            // double latitude = mapView.getMapCenter().getLatitudeE6() * 1E-6;
	            // double ppm = (mapView.getProjection().metersToEquatorPixels(1) / (Math.cos(Math.toRadians(latitude)))); // pixels per meter
	            // int zoom = 15;
	            // mapControl.setZoom(zoom);
	            // mapControl.zoomToSpan(latSpanE6, lonSpanE6);
	            // mapControl.zoomIn();
	            // mapControl.setZoom(19);
        	}
        }
    }
    
    @Override
    public void onResume() {
        super.onResume();
        paused = false;
        update();
    }

    @Override
    public void onPause() {
        super.onPause();
        paused = true;
    }

}


