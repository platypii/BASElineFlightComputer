package com.platypii.baseline.lasers;

import androidx.annotation.NonNull;
import com.google.gson.reflect.TypeToken;
import com.platypii.baseline.util.LocalCache;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Local laser profile list cache
 */
public class LaserCache extends LocalCache<LaserProfile> {

    public LaserCache(@NonNull String cacheName) {
        super("cloud.laser." + cacheName);
    }

    /**
     * Return Type of List<LaserProfile>
     */
    @NonNull
    @Override
    public Type listType() {
        return new TypeToken<List<LaserProfile>>(){}.getType();
    }

    /**
     * Return the unique id for an item
     */
    @NonNull
    @Override
    public String getId(@NonNull LaserProfile item) {
        return item.laser_id;
    }

}
