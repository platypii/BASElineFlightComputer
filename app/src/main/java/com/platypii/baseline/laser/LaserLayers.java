package com.platypii.baseline.laser;

import com.platypii.baseline.events.ProfileLayerEvent;
import com.platypii.baseline.views.charts.layers.ProfileLayer;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.greenrobot.eventbus.EventBus;

/**
 * Class to maintain the list of currently selected laser profile layers
 * TODO: Persist
 */
public class LaserLayers {
    private static final String TAG = "LaserLayers";
    @Nullable
    private static LaserLayers instance;
    public static LaserLayers getInstance() {
        if (instance == null) {
            instance = new LaserLayers();
        }
        return instance;
    }

    public final List<ProfileLayer> layers = new ArrayList<>();

    public void add(@NonNull ProfileLayer layer) {
        if (layers.contains(layer)) {
            // Don't add duplicate layer
            return;
        }
        layers.add(layer);
        EventBus.getDefault().post(new ProfileLayerEvent.ProfileLayerAdded(layer));
    }

    public void update(@NonNull ProfileLayer layer) {
        EventBus.getDefault().post(new ProfileLayerEvent.ProfileLayerUpdated(layer));
    }

    public void remove(@NonNull ProfileLayer layer) {
        if (layers.remove(layer)) {
            EventBus.getDefault().post(new ProfileLayerEvent.ProfileLayerRemoved(layer));
        } else {
            Log.e(TAG, "Remove called on unknown layer");
        }
    }

    public void removeById(@NonNull String id) {
        for (Iterator<ProfileLayer> it = layers.iterator(); it.hasNext(); ) {
            final ProfileLayer layer = it.next();
            if (layer.id().equals(id)) {
                it.remove();
                EventBus.getDefault().post(new ProfileLayerEvent.ProfileLayerRemoved(layer));
            }
        }
    }

}
