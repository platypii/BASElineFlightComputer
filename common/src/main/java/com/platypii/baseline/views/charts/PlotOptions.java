package com.platypii.baseline.views.charts;

import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.IntBounds;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Used by PlotView to represent screen padding, etc.
 */
public class PlotOptions {

    // Plot will scale to data bounds, with some padding on the sides
    public final IntBounds padding = new IntBounds();

    // Drawing options
    public final float density;
    final int axis_color = 0xffee0000;
    final int grid_color = 0xff555555;
    final int grid_text_color = 0xff666666;
    final float font_size = 16;

    // Axis options
    final AxesOptions axis = new AxesOptions();

    // Constructor requires density
    PlotOptions(float density) {
        this.density = density;
    }

    // Axis options
    static class AxesOptions {
        @NonNull
        AxisOptions x = new AxisOptions();
        @NonNull
        AxisOptions y = new AxisOptions();
    }

    static class AxisOptions {
        // Major units are the smallest unit that will get a major grid line
        // We sometimes use a multiple of major units too
        double major_units = 1;

        // Override this to change how labels are displayed
        @Nullable
        public String format(double value) {
            return null;
        }
    }

    @NonNull
    static AxisOptions axisDistance() {
        return new PlotOptions.AxisOptions() {
            {
                major_units = Convert.metric ? 1 : Convert.FT;
            }

            @Override
            public String format(double value) {
                return Math.abs(value) < 0.1 ? "" : Convert.distance(Math.abs(value), 0, true);
            }
        };
    }

    @NonNull
    static AxisOptions axisTime() {
        return new PlotOptions.AxisOptions() {
            private final SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss", Locale.US);

            {
                major_units = 60000; // 1 Minute
            }

            @Override
            public String format(double value) {
                return format.format((long) value);
            }
        };
    }

    @NonNull
    static AxisOptions axisSpeed() {
        return new PlotOptions.AxisOptions() {
            {
                major_units = Convert.metric ? Convert.KPH : Convert.MPH;
            }

            @Override
            public String format(double value) {
                final double absValue = Math.abs(value);
                return absValue < 0.1 ? "" : Convert.speed(absValue, 0, true);
            }
        };
    }

}
