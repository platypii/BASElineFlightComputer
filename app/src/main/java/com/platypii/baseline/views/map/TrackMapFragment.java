package com.platypii.baseline.views.map;

import com.platypii.baseline.events.ChartFocusEvent;
import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.views.map.layers.FocusLayer;
import com.platypii.baseline.views.map.layers.PlacesLayer;
import com.platypii.baseline.views.map.layers.TrackLayer;
import com.platypii.baseline.views.tracks.TrackDataActivity;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TrackMapFragment extends BaseMapFragment {

    @Nullable
    private FocusLayer focusLayer;

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        super.onMapReady(map);

        // Get track data from parent activity
        final Activity parent = getActivity();
        if (parent instanceof TrackDataActivity) {
            ((TrackDataActivity) parent).trackData.thenAccept(trackData -> loadTrack(map, trackData));
        }

        // Add map layers
        addLayer(new PlacesLayer(getLayoutInflater()));
        updateLayers();
    }

    private void loadTrack(@NonNull GoogleMap map, @NonNull TrackData trackData) {
        addLayer(new TrackLayer(trackData));
        addLayer(focusLayer = new FocusLayer(trackData));
        if (trackData.stats.bounds != null) {
            final float density = getResources().getDisplayMetrics().density;
            map.moveCamera(CameraUpdateFactory.newLatLngBounds(trackData.stats.bounds, (int) (20 * density)));
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTrackFocus(@NonNull ChartFocusEvent.TrackFocused event) {
        if (focusLayer != null) {
            focusLayer.setFocus(event.location);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnFocus(@NonNull ChartFocusEvent.Unfocused event) {
        if (focusLayer != null) {
            focusLayer.setFocus(null);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

}
