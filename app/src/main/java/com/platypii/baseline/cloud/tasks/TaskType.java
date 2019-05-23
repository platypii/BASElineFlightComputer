package com.platypii.baseline.cloud.tasks;

public abstract class TaskType {

    public static final TaskType laserUpload = new LaserTaskType();
    public static final TaskType trackUpload = new TrackTaskType();

    /**
     * The name of this type of task
     */
    abstract String name();

    public static class LaserTaskType extends TaskType {
        @Override
        String name() {
            return "LaserUpload";
        }
    }

    public static class TrackTaskType extends TaskType {
        @Override
        String name() {
            return "TrackUpload";
        }
    }

}
