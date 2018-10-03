package com.platypii.baseline.events;

import com.platypii.baseline.tracks.TrackFile;
import android.support.annotation.Nullable;

/**
 * Indicates that logging has either started or stopped
 */
public class LoggingEvent {

    public final boolean started;
    @Nullable
    public final TrackFile trackFile;

    public LoggingEvent(boolean started, @Nullable TrackFile trackFile) {
        this.started = started;
        this.trackFile = trackFile;
        if (started ^ trackFile == null) {
            throw new IllegalStateException("Invalid logging event " + started + " " + trackFile);
        }
    }

}
