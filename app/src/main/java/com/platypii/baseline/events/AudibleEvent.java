package com.platypii.baseline.events;

/**
 * Indicates that audible has either started or stopped
 */
public class AudibleEvent {

    public final boolean started;

    public AudibleEvent(boolean started) {
        this.started = started;
    }

}
