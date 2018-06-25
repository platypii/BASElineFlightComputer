package com.platypii.baseline.events;

import com.platypii.baseline.tracks.TrackFile;

/**
 * Indicates that logging has either started or stopped
 */
public class LoggingEvent {

    public final boolean started;
    public final TrackFile trackFile;

    public LoggingEvent(boolean started, TrackFile trackFile) {
        this.started = started;
        this.trackFile = trackFile;
        if (started ^ trackFile == null) {
            throw new IllegalStateException("Invalid logging event " + started + " " + trackFile);
        }
    }

}
