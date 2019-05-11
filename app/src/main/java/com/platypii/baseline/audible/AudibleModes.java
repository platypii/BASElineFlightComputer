package com.platypii.baseline.audible;

import com.platypii.baseline.audible.modes.GlideRatioMode;
import com.platypii.baseline.audible.modes.HorizontalSpeedMode;
import com.platypii.baseline.audible.modes.NavigationMode;
import com.platypii.baseline.audible.modes.TotalSpeedMode;
import com.platypii.baseline.audible.modes.VerticalSpeedMode;
import com.platypii.baseline.util.Exceptions;
import androidx.annotation.NonNull;

/**
 * Contains the default audible modes
 */
class AudibleModes {

    private static final AudibleMode horizontal_speed = new HorizontalSpeedMode();
    private static final AudibleMode vertical_speed = new VerticalSpeedMode();
    private static final AudibleMode total_speed = new TotalSpeedMode();
    private static final AudibleMode glide_ratio = new GlideRatioMode();
    private static final AudibleMode navigation = new NavigationMode();

    @NonNull
    public static AudibleMode get(@NonNull String audibleMode) {
        switch (audibleMode) {
            case "horizontal_speed":
                return horizontal_speed;
            case "vertical_speed":
                return vertical_speed;
            case "total_speed":
                return total_speed;
            case "glide_ratio":
                return glide_ratio;
            case "navigation":
                return navigation;
            default:
                Exceptions.report(new IllegalStateException("Invalid audible mode " + audibleMode));
                return horizontal_speed;
        }
    }

}
