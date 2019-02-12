package com.platypii.baseline.events;

import com.platypii.baseline.views.charts.layers.ProfileLayer;

/**
 * Indicates that an exit profile has been added, removed, or updated
 */
public abstract class ProfileLayerEvent {

    public static class ProfileLayerAdded extends ProfileLayerEvent {
        public final ProfileLayer layer;
        public ProfileLayerAdded(ProfileLayer layer) {
            this.layer = layer;
        }
    }
    public static class ProfileLayerUpdated extends ProfileLayerEvent {
        public final ProfileLayer layer;
        public ProfileLayerUpdated(ProfileLayer layer) {
            this.layer = layer;
        }
    }
    public static class ProfileLayerRemoved extends ProfileLayerEvent {
        public final ProfileLayer layer;
        public ProfileLayerRemoved(ProfileLayer layer) {
            this.layer = layer;
        }
    }

}
