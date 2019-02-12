package com.platypii.baseline.laser;

import com.platypii.baseline.events.ProfileLayerEvent;
import com.platypii.baseline.views.charts.layers.ProfileLayer;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

/**
 * Class to maintain the list of currently selected laser profile layers
 * TODO: Persist
 */
public class LaserLayers {
    private static final String TAG = "LaserLayers";
    private static LaserLayers instance;
    public static LaserLayers getInstance() {
        if (instance == null) {
            instance = new LaserLayers();
        }
        return instance;
    }

    public final List<ProfileLayer> layers = new ArrayList<>();

    public void add(ProfileLayer layer) {
        if (layers.contains(layer)) {
            // Don't add duplicate layer
            return;
        }
        layers.add(layer);
        EventBus.getDefault().post(new ProfileLayerEvent.ProfileLayerAdded(layer));
    }

    public void update(ProfileLayer layer) {
        EventBus.getDefault().post(new ProfileLayerEvent.ProfileLayerUpdated(layer));
    }

    public void remove(ProfileLayer layer) {
        layers.remove(layer);
        if (layers.remove(layer)) {
            EventBus.getDefault().post(new ProfileLayerEvent.ProfileLayerRemoved(layer));
        } else {
            Log.e(TAG, "Remove called on unknown layer");
        }
    }

}
