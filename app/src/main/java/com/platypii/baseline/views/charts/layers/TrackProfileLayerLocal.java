package com.platypii.baseline.views.charts.layers;

import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.tracks.TrackFile;
import androidx.annotation.NonNull;

public class TrackProfileLayerLocal extends TrackProfileLayer {
    @NonNull
    public final TrackFile track;

    public TrackProfileLayerLocal(@NonNull TrackFile track) {
        super(track.getName(), new TrackData(track.file), Colors.nextColor());
        this.track = track;
    }

}
