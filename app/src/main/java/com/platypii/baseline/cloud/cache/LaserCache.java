package com.platypii.baseline.cloud.cache;

import com.platypii.baseline.laser.LaserProfile;
import android.support.annotation.NonNull;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Local laser profile list cache
 */
public class LaserCache extends LocalCache<LaserProfile> {

    @Override
    String keyPrefix() {
        return "cloud.laser";
    }

    /**
     * Return Type of List<LaserProfile>
     */
    @Override
    Type listType() {
        return new TypeToken<List<LaserProfile>>(){}.getType();
    }

    /**
     * Return the unique id for an item
     */
    @Override
    String getId(@NonNull LaserProfile item) {
        return item.laser_id;
    }

}
