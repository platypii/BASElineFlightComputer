package com.platypii.baseline.views.charts;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.util.Bounds;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.DataSeries;
import com.platypii.baseline.util.Dates;
import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import java.util.List;

public class TimeChart extends PlotView {

    private final TrackFile trackFile;
    private final List<MLocation> trackData;

    private final DataSeries altitudeSeries = new DataSeries();

    public TimeChart(Context context, @NonNull TrackFile trackFile) {
        super(context, null);

        // Slower refresh rate
        refreshRateMillis = 200;

        // Get track data
        this.trackFile = trackFile;
        trackData = trackFile.getData();

        final float density = getResources().getDisplayMetrics().density;
        options.padding.top = (int) (12 * density);
        options.padding.bottom = (int) (42 * density);
        options.padding.left = (int) (density);
        options.padding.right = (int) (76 * density);

        options.axis.x = new PlotOptions.AxisOptions() {
            @Override
            public String format(double value) {
                return Dates.chartDate((long) value);
            }
        };
        options.axis.x.major_units = 1;
        options.axis.y.major_units = Convert.metric? Convert.KPH : Convert.MPH;
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
        // TODO: Draw on multiple axes
        // Update data series
        altitudeSeries.reset();
        for(MLocation loc : trackData) {
            altitudeSeries.addPoint(loc.millis, loc.altitude_gps);
        }
        // Draw data series
        paint.setColor(0xffff0000);
        drawLine(plot, altitudeSeries, 2, paint);
//        drawLine(canvas, speedSeries, 2, 0xff0000ff);
//        drawLine(canvas, glideSeries, 2, 0xff6f00ff);
    }

    @NonNull
    @Override
    public Bounds getBounds(@NonNull Bounds dataBounds) {
        // Scale to match data
        return dataBounds;
    }

}
