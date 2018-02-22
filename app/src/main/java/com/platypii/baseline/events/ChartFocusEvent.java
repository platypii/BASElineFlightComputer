package com.platypii.baseline.events;

import com.platypii.baseline.measurements.MLocation;
import android.support.annotation.Nullable;

/**
 * Indicates that audible has either started or stopped
 */
public class ChartFocusEvent {

    public final MLocation location;

    public ChartFocusEvent(@Nullable MLocation location) {
        this.location = location;
    }

}
