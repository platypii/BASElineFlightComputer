package com.platypii.baseline.views.charts;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.util.Bounds;
import com.platypii.baseline.util.DataSeries;
import com.platypii.baseline.views.charts.layers.FlightModeLayer;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TimeChart extends PlotView {

    private static final int AXIS_ALT = 0;
    private static final int AXIS_SPEED = 1;
    private static final int AXIS_GLIDE = 2;

    @Nullable
    TrackData trackData;

    private final DataSeries altitudeSeries = new DataSeries();
    private final DataSeries speedSeries = new DataSeries();
    private final DataSeries glideSeries = new DataSeries();

    @Nullable
    private MLocation focus;

    public TimeChart(Context context, AttributeSet attrs) {
        super(context, attrs);

        final float density = getResources().getDisplayMetrics().density;
        options.padding.top = (int) (18 * density);
        options.padding.bottom = (int) (6 * density);

        options.axis.x = PlotOptions.axisTime();
        options.axis.y = PlotOptions.axisDistance();

        // Initialize bounds with 3 axes
        plot.initBounds(3);
    }

    public void loadTrack(@NonNull TrackData trackData) {
        this.trackData = trackData;

        // Load track data into individual time series
        altitudeSeries.reset();
        speedSeries.reset();
        glideSeries.reset();
        for (MLocation loc : trackData.data) {
            altitudeSeries.addPoint(loc.millis, loc.altitude_gps);
            speedSeries.addPoint(loc.millis, loc.totalSpeed());
            glideSeries.addPoint(loc.millis, glide(loc));
        }

        addLayer(new FlightModeLayer(trackData));
    }

    /**
     * Only use "good" glide numbers
     */
    private double glide(@NonNull MLocation point) {
        final double glide = point.glideRatio();
        if (point.groundSpeed() > 3.5 && point.climb < 0.5 && point.climb != 0.0 && 0 <= glide && glide <= 4) {
            return glide;
        } else {
            return Double.NaN;
        }
    }

    public void onFocus(@Nullable MLocation focus) {
        this.focus = focus;
        invalidate();
    }

    @Override
    public void drawData(@NonNull Plot plot) {
        if (trackData != null) {
            if (trackData.data.isEmpty()) {
                plot.text.setTextAlign(Paint.Align.CENTER);
                //noinspection IntegerDivisionInFloatingPointContext
                plot.canvas.drawText("no track data", plot.width / 2, plot.height / 2, plot.text);
            } else {
                // Draw track data
                drawTrackData(plot);
            }
        }
        // Draw focus line
        if (focus != null) {
            final float sx = plot.getX(0, focus.millis);
            plot.paint.setColor(0xddeeeeee);
            plot.paint.setStrokeWidth(3f);
            plot.canvas.drawLine(sx, 0, sx, getHeight(), plot.paint);
        }
    }

    /**
     * Draw track data points
     */
    private void drawTrackData(@NonNull Plot plot) {
        // Draw data series
        plot.paint.setColor(0xffff0000);
        plot.drawLine(AXIS_ALT, altitudeSeries, 1.2f);
        plot.paint.setColor(0xff0000ff);
        plot.drawLine(AXIS_SPEED, speedSeries, 1.2f);
        plot.paint.setColor(0xff109bbf);
        plot.drawLine(AXIS_GLIDE, glideSeries, 1.2f);
    }

    @NonNull
    @Override
    public Bounds getBounds(@NonNull Bounds dataBounds, int axis) {
        if (axis == AXIS_GLIDE) {
            dataBounds.y.min = 0;
            dataBounds.y.max = 4;
        }
        // Else scale to match data
        return dataBounds;
    }

}
