package com.platypii.baseline.events;

/**
 * Indicates that logging has either started or stopped
 */
public class LoggingEvent {

    public final boolean started;

    public LoggingEvent(boolean started) {
        this.started = started;
    }

}
