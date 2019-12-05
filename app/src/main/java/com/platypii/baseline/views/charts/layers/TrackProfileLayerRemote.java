package com.platypii.baseline.views.charts.layers;

import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.tracks.TrackMetadata;

import androidx.annotation.NonNull;

/**
 * TrackProfileLayer for remote track
 */
public class TrackProfileLayerRemote extends TrackProfileLayer {
    @NonNull
    public final TrackMetadata track;

    public TrackProfileLayerRemote(@NonNull TrackMetadata track, @NonNull TrackData trackData) {
        super(track.track_id, track.getName(), trackData, Colors.nextColor());
        this.track = track;
    }

}
