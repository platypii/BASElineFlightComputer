package com.platypii.baseline.tracks;

import androidx.annotation.NonNull;

public class TrackSearch {

    /**
     * Return true if the track matches the search filter string
     * TODO: Search track.stats.plan.name
     */
    public static boolean matchTrack(@NonNull TrackMetadata track, @NonNull String filter) {
        // Make a lower case super string of all properties we want to search
        final StringBuilder sb = new StringBuilder();
        if (track.place != null) {
            sb.append(track.place.name);
            sb.append(' ');
            sb.append(track.place.region);
            sb.append(' ');
            sb.append(track.place.country);
            sb.append(' ');
            sb.append(track.place.objectType);
            if (track.place.wingsuitable) {
                sb.append(" wingsuit");
            }
            if ("DZ".equals(track.place.objectType)) {
                sb.append(" skydive");
            }
            if (track.place.isBASE()) {
                sb.append(" BASE");
            }
        }
        if (track.suit != null) {
            sb.append(' ').append(track.suit);
        }
        if (track.canopy != null) {
            sb.append(' ').append(track.canopy);
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
