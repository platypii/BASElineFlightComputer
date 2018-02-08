package com.platypii.baseline.views.charts;

import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.IntBounds;
import android.support.annotation.Nullable;

/**
 * Used by PlotView to represent screen padding, etc.
 */
class PlotOptions {

    // Plot will scale to data bounds, with some padding on the sides
    public final IntBounds padding = new IntBounds();

    // Drawing options
    final float density;
    final int axis_color = 0xffee0000;
    final int grid_color = 0xff555555;
    final float font_size = 16;

    // Axis options
    final AxesOptions axis = new AxesOptions();

    // Constructor requires density
    PlotOptions(float density) {
        this.density = density;
    }

    // Axis options
    static class AxesOptions {
        AxisOptions x = new AxisOptions();
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

    static AxisOptions axisTime() {
        return new PlotOptions.AxisOptions() {
            {
                major_units = Convert.metric? Convert.KPH : Convert.MPH;
            }
            @Override
            public String format(double value) {
                return Math.abs(value) < 0.1 ? "" : Convert.speed(value, 0, true);
            }
        };
    }

    static AxisOptions axisSpeed() {
        return new PlotOptions.AxisOptions() {
            {
                major_units = Convert.metric? Convert.KPH : Convert.MPH;
            }
            @Override
            public String format(double value) {
                return Math.abs(value) < 0.1 ? "" : Convert.speed(value, 0, true);
            }
        };
    }

}
