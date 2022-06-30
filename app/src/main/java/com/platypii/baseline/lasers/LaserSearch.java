package com.platypii.baseline.lasers;

import androidx.annotation.NonNull;

import static com.platypii.baseline.util.StringUtil.normalize;

public class LaserSearch {

    /**
     * Return true if the laser profile matches the search filter string
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
        final String superString = normalize(sb.toString());
        // Break into tokens
        for (String token : normalize(filter).split(" ")) {
            if (!superString.contains(token)) {
                return false;
            }
        }
        return true;
    }

}
