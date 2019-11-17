package com.platypii.baseline.views.charts;

import com.platypii.baseline.util.Convert;

import java.util.TimeZone;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PlotsOptionsTest {

    @Test
    public void plotOptions() {
        PlotOptions options = new PlotOptions(1);
        assertEquals(0xffee0000, options.axis_color);
        assertEquals(0xff555555, options.grid_color);
    }

    @Test
    public void axisDefault() {
        PlotOptions.AxisOptions axis = new PlotOptions.AxisOptions();
        assertEquals(1, axis.major_units, 0.001);
        assertNull(axis.format(100));
    }

    @Test
    public void axisDistance() {
        Convert.metric = false;
        PlotOptions.AxisOptions axis = PlotOptions.axisDistance();
        assertEquals(0.3048, axis.major_units, 0.001);
        assertEquals("328 ft", axis.format(100));
    }

    @Test
    public void axisSpeed() {
        Convert.metric = false;
        PlotOptions.AxisOptions axis = PlotOptions.axisSpeed();
        assertEquals(0.44704, axis.major_units, 0.001);
        assertEquals("224 mph", axis.format(100));
    }

    @Test
    public void axisTime() {
        TimeZone.setDefault(TimeZone.getTimeZone("Pacific/Honolulu"));
        PlotOptions.AxisOptions axis = PlotOptions.axisTime();
        assertEquals(60000, axis.major_units, 0.001);
        assertEquals("16:20:00", axis.format(8400000));
    }

}
