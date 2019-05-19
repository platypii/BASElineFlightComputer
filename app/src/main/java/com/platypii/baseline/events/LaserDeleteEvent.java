package com.platypii.baseline.events;

/**
 * Indicates that a laser was deleted
 */
public abstract class LaserDeleteEvent {

    public static class LaserDeleteSuccess extends LaserDeleteEvent {
        public final String laser_id;
        public LaserDeleteSuccess(String laser_id) {
            this.laser_id = laser_id;
        }
    }
    public static class LaserDeleteFailure extends LaserDeleteEvent {
        public final String laser_id;
        public LaserDeleteFailure(String laser_id, String error) {
            this.laser_id = laser_id;
        }
    }

}
