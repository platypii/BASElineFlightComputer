package com.platypii.baseline.views.charts;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.tracks.TrackFileData;
import com.platypii.baseline.util.Bounds;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.DataSeries;
import com.platypii.baseline.util.Dates;
import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import java.io.File;
import java.util.List;

public class TimeChart extends PlotView {

    private static final int AXIS_ALT = 0;
    private static final int AXIS_SPEED = 1;
    private static final int AXIS_GLIDE = 2;

    private File trackFile;

    private final DataSeries altitudeSeries = new DataSeries();
    private final DataSeries speedSeries = new DataSeries();
    private final DataSeries glideSeries = new DataSeries();

    public TimeChart(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Slower refresh rate
        refreshRateMillis = 1000;

        final float density = getResources().getDisplayMetrics().density;
        options.padding.top = (int) (4 * density);
        options.padding.bottom = (int) (4 * density);

        options.axis.x = new PlotOptions.AxisOptions() {
            @Override
            public String format(double value) {
                return Dates.chartDate((long) value);
            }
        };
        options.axis.x.major_units = 1;
        options.axis.y.major_units = Convert.metric? Convert.KPH : Convert.MPH;

        // Initialize bounds with 3 axes
        plot.initBounds(3);
    }

    public void loadTrack(File trackFile) {
        // Load track data from file
        this.trackFile = trackFile;
        final List<MLocation> trackData = TrackFileData.getTrackData(trackFile);

        // Load track data into individual time series
        altitudeSeries.reset();
        speedSeries.reset();
        glideSeries.reset();
        for(MLocation loc : trackData) {
            altitudeSeries.addPoint(loc.millis, loc.altitude_gps);
            speedSeries.addPoint(loc.millis, loc.totalSpeed());
            glideSeries.addPoint(loc.millis, loc.glideRatio());
        }
    }

    @Override
    public void drawData(@NonNull Plot plot) {
        if (trackFile != null) {
            // Draw track data
            drawTrackData(plot);
        } else {
            // Draw "no track file"
            text.setTextAlign(Paint.Align.CENTER);
            plot.canvas.drawText("no track file", plot.width / 2, plot.height / 2, text);
        }
    }

    /**
     * Draw track data points
     */
    private void drawTrackData(@NonNull Plot plot) {
        // Draw data series
        paint.setColor(0xffff0000);
        plot.drawLine(AXIS_ALT, altitudeSeries, 1.5f, paint);
        paint.setColor(0xff0000ff);
        plot.drawLine(AXIS_SPEED, speedSeries, 1.5f, paint);
        paint.setColor(0xff7f00ff);
        plot.drawLine(AXIS_GLIDE, glideSeries, 1.5f, paint);
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
