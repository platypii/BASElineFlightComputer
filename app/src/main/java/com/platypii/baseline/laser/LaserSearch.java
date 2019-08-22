package com.platypii.baseline.laser;

import androidx.annotation.NonNull;

public class LaserSearch {

    /**
     * Return true if the track matches the search filter string
     */
    public static boolean matchLaser(@NonNull LaserProfile laser, @NonNull String filter) {
        // Make a lower case super string of all properties we want to search
        final StringBuilder sb = new StringBuilder();
        sb.append(laser.name);
        sb.append(' ');
        if (laser.place != null) {
            sb.append(laser.place.name);
            sb.append(' ');
            sb.append(laser.place.region);
            sb.append(' ');
            sb.append(laser.place.country);
        }
        final String superString = sb.toString().toLowerCase();
        // Break into tokens
        for (String token : filter.toLowerCase().split(" ")) {
            if (!superString.contains(token)) {
                return false;
            }
        }
        return true;
    }

}
