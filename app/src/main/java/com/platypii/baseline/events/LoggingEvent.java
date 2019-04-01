package com.platypii.baseline.events;

import com.platypii.baseline.tracks.TrackFile;
import android.support.annotation.NonNull;

/**
 * Indicates that logging has either started or stopped
 */
public abstract class LoggingEvent {

    public boolean started;

    public static class LoggingStart extends LoggingEvent {
        public LoggingStart() {
            this.started = true;
        }
    }

    public static class LoggingStop extends LoggingEvent {
        @NonNull
        public final TrackFile trackFile;
        public LoggingStop(@NonNull TrackFile trackFile) {
            this.started = false;
            this.trackFile = trackFile;
            if (started ^ trackFile == null) {
                throw new IllegalStateException("Invalid logging event " + started + " " + trackFile);
            }
        }
    }

}
