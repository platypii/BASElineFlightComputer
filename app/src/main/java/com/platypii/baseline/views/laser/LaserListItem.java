package com.platypii.baseline.views.laser;

import com.platypii.baseline.laser.LaserProfile;

public abstract class LaserListItem {

    // Item types
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_LASER = 1;

    abstract public int getType();

    public static class ListHeader extends LaserListItem {
        final String name;
        ListHeader(String name) {
            this.name = name;
        }
        @Override
        public int getType() {
            return TYPE_HEADER;
        }
    }

    public static class ListLaser extends LaserListItem {
        public final LaserProfile laser;
        ListLaser(LaserProfile laser) {
            this.laser = laser;
        }
        @Override
        public int getType() {
            return TYPE_LASER;
        }
    }

}
