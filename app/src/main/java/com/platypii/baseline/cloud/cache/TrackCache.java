package com.platypii.baseline.cloud.cache;

import com.platypii.baseline.cloud.CloudData;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Local track list cache
 */
public class TrackCache extends LocalCache<CloudData> {

    @Override
    String keyPrefix() {
        return "cloud.track";
    }

    /**
     * Return Type of List<CloudData>
     */
    @Override
    Type listType() {
        return new TypeToken<List<CloudData>>(){}.getType();
    }

    /**
     * Return the unique id for an item
     */
    @Override
    String getId(CloudData item) {
        return item.track_id;
    }

}
