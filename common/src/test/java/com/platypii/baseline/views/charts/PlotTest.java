package com.platypii.baseline.views.charts;

import android.graphics.Canvas;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PlotTest {

    @Test
    public void plot() {
        PlotOptions options = new PlotOptions(1, 1);
        Plot plot = new Plot(options);
        plot.initBounds(1);
        assertNull(plot.canvas);
        Canvas canvas = new Canvas();
        plot.setCanvas(canvas);
        assertNotNull(plot.canvas);
    }

}
