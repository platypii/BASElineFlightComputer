package com.platypii.baseline.cloud.cache;

import com.platypii.baseline.tracks.TrackMetadata;

import androidx.annotation.NonNull;
import com.google.gson.reflect.TypeToken;
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
    Type listType() {
        return new TypeToken<List<TrackMetadata>>(){}.getType();
    }

    /**
     * Return the unique id for an item
     */
    @NonNull
    @Override
    String getId(@NonNull TrackMetadata item) {
        return item.track_id;
    }

}
