package com.platypii.baseline.views.charts.layers;

import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.tracks.TrackData;
import androidx.annotation.NonNull;

public class TrackProfileLayerRemote extends TrackProfileLayer {
    public final CloudData track;

    public TrackProfileLayerRemote(@NonNull CloudData track, @NonNull TrackData trackData) {
        super(track.getName(), trackData, Colors.nextColor());
        this.track = track;
    }

}
