package com.platypii.baseline.tracks;

import androidx.annotation.NonNull;
import com.google.gson.reflect.TypeToken;
import com.platypii.baseline.util.LocalCache;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Local track list cache
 */
public class TrackCache extends LocalCache<TrackMetadata> {

    public TrackCache() {
        super("cloud.track");
    }

    /**
     * Return Type of List<TrackMetadata>
     */
    @NonNull
    @Override
    public Type listType() {
        return new TypeToken<List<TrackMetadata>>(){}.getType();
    }

    /**
     * Return the unique id for an item
     */
    @NonNull
    @Override
    public String getId(@NonNull TrackMetadata item) {
        return item.track_id;
    }

}
