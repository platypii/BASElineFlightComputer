package com.platypii.baseline.views.tracks;

import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.tracks.TrackFile;

public abstract class TrackListItem {

    // Item types
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_TRACK_LOCAL = 1;
    public static final int TYPE_TRACK_REMOTE = 2;

    abstract public int getType();

    public static class ListHeader extends TrackListItem {
        final String name;
        ListHeader(String name) {
            this.name = name;
        }
        @Override
        public int getType() {
            return TYPE_HEADER;
        }
    }

    public static class ListTrackFile extends TrackListItem {
        public final TrackFile track;
        ListTrackFile(TrackFile track) {
            this.track = track;
        }
        @Override
        public int getType() {
            return TYPE_TRACK_LOCAL;
        }
    }

    public static class ListTrackData extends TrackListItem {
        public final CloudData track;
        ListTrackData(CloudData track) {
            this.track = track;
        }
        @Override
        public int getType() {
            return TYPE_TRACK_REMOTE;
        }
    }

}
