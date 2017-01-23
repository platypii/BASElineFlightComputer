package com.platypii.baseline.location;

import com.platypii.baseline.R;

/**
 * Represents the current state of GPS, including bluetooth info
 */
public class LocationStatus {
    private static final String TAG = "LocationStatus";

    public final String message;
    public final int iconColor;

    private static final int[] icons = {
            R.drawable.warning,
            R.drawable.status_red,
            R.drawable.status_yellow,
            R.drawable.status_green,
            R.drawable.status_blue
    };

    public LocationStatus(String message, int iconColor) {
        this.message = message;
        this.iconColor = iconColor;
    }

    public int icon() {
        return icons[iconColor];
    }

}
