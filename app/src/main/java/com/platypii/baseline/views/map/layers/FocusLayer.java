package com.platypii.baseline.views.map.layers;

import com.platypii.baseline.R;
import com.platypii.baseline.events.ChartFocusEvent;
import com.platypii.baseline.location.Geo;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.tracks.TrackData;

import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import org.greenrobot.eventbus.EventBus;

public class FocusLayer extends MapLayer {
    private static final String TAG = "FocusLayer";

    @NonNull
    private final TrackData trackData;

    @Nullable
    private Marker focusMarker;

    public FocusLayer(@NonNull TrackData trackData) {
        this.trackData = trackData;
    }

    @Override
    public void onAdd(@NonNull GoogleMap map) {
        // Add home location pin
        focusMarker = map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .visible(false)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pin))
                .anchor(0.5f, 1.0f)
        );
    }

    public void setFocus(@Nullable MLocation focus) {
        if (focusMarker != null) {
            if (focus != null) {
                Log.i(TAG, "Focusing track map on " + focus);
                focusMarker.setPosition(focus.latLng());
                focusMarker.setVisible(true);
            } else {
                focusMarker.setVisible(false);
            }
        }
    }

    @Override
    public void onMapClick(LatLng focus) {
        EventBus.getDefault().post(findClosest(focus));
    }

    /**
     * Performs a search for the nearest data point
     */
    @NonNull
    private ChartFocusEvent findClosest(@Nullable LatLng focus) {
        ChartFocusEvent closest = new ChartFocusEvent.Unfocused();
        if (focus != null && !trackData.data.isEmpty()) {
            double closestDistance = Double.POSITIVE_INFINITY;
            for (MLocation loc : trackData.data) {
                final double distance = Geo.fastDistance(loc.latitude, loc.longitude, focus.latitude, focus.longitude);
                if (distance < closestDistance) {
                    closest = new ChartFocusEvent.TrackFocused(loc, trackData.data);
                    closestDistance = distance;
                }
            }
        }
        return closest;
    }


}
