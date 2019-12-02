package com.platypii.baseline.events;

import com.platypii.baseline.laser.LaserMeasurement;
import com.platypii.baseline.measurements.MLocation;

import androidx.annotation.NonNull;
import java.util.List;

/**
 * Indicates that user has touched a chart
 */
public class ChartFocusEvent {

    public static class TrackFocused extends ChartFocusEvent {
        @NonNull
        public final MLocation location;
        @NonNull
        public final List<MLocation> track;

        public TrackFocused(@NonNull MLocation location, @NonNull List<MLocation> track) {
            this.location = location;
            this.track = track;
        }
    }

    public static class LaserFocused extends ChartFocusEvent {
        @NonNull
        public final LaserMeasurement point;

        public LaserFocused(@NonNull LaserMeasurement point) {
            this.point = point;
        }
    }

    public static class Unfocused extends ChartFocusEvent {
    }

    private ChartFocusEvent() {
    }

}
