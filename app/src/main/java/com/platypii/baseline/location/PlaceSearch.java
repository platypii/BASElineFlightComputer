package com.platypii.baseline.location;

import androidx.annotation.NonNull;

import com.platypii.baseline.places.Place;

import static com.platypii.baseline.util.StringUtil.normalize;

public class PlaceSearch {

    /**
     * Return true if the place matches the search filter string
     */
    public static boolean matchPlace(@NonNull Place place, @NonNull String filter) {
        // Make a lower case super string of all properties we want to search
        final StringBuilder sb = new StringBuilder();
        sb.append(place.name);
        sb.append(' ');
        sb.append(place.region);
        sb.append(' ');
        sb.append(place.country);
        sb.append(' ');
        sb.append(place.objectType);

        if (place.wingsuitable) {
            sb.append(" wingsuit");
        }
        if (place.isSkydive()) {
            sb.append(" skydive");
        }
        if (place.isBASE()) {
            sb.append(" BASE");
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
