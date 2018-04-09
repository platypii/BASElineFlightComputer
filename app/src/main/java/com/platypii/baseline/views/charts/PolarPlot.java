package com.platypii.baseline.views.charts;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.AdjustBounds;
import com.platypii.baseline.util.Bounds;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.DataSeries;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import java.util.List;

public class PolarPlot extends PlotView {

    private static final int AXIS_POLAR = 0;
    private final Bounds bounds = new Bounds();

    private List<MLocation> trackData;

    private final DataSeries speedSeries = new DataSeries();

    final Bounds inner = new Bounds();
    final Bounds outer = new Bounds();

    private MLocation focus;

    public PolarPlot(Context context, AttributeSet attrs) {
        super(context, attrs);

        final float density = getResources().getDisplayMetrics().density;
        options.padding.top = (int) (12 * density);
        options.padding.bottom = (int) (42 * density);
        options.padding.left = (int) (density);
        options.padding.right = (int) (76 * density);

        inner.x.min = outer.x.min = 0;
        inner.x.max = 9 * Convert.MPH;
        outer.x.max = 160 * Convert.MPH;
        inner.y.min = -160 * Convert.MPH;
        outer.y.min = -2 * Convert.MPH;
        inner.y.max = 0;
        outer.y.max = 28 * Convert.MPH;

        options.axis.x = options.axis.y = PlotOptions.axisSpeed();

        paint.setStrokeJoin(Paint.Join.ROUND);
    }

    public void loadTrack(List<MLocation> trackData) {
        this.trackData = trackData;

        // Load track data into time series
        speedSeries.reset();
        for (MLocation loc : trackData) {
            speedSeries.addPoint(loc.groundSpeed(), loc.climb);
        }
    }

    public void onFocus(@Nullable MLocation focus) {
        this.focus = focus;
        invalidate();
    }

    @Override
    public void drawData(@NonNull Plot plot) {
        if (trackData != null) {
            if (trackData.isEmpty()) {
                text.setTextAlign(Paint.Align.CENTER);
                plot.canvas.drawText("no track data", plot.width / 2, plot.height / 2, text);
            } else {
                // Draw background ellipses
                drawEllipses(plot);

                // Draw data
                paint.setColor(0xff7f00ff);
                plot.drawLine(AXIS_POLAR, speedSeries, 1.5f, paint);
            }
        }
        // Draw focus
        if (focus != null) {
            final double x = focus.groundSpeed();
            final double y = focus.climb;
            paint.setColor(0xcceeeeee);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(options.density);
            plot.drawPoint(0, x, y, 2 * options.density, paint);
            paint.setStyle(Paint.Style.FILL);
            plot.drawPoint(0, x, y, options.density, paint);
        }
    }

    private final BlurMaskFilter blurry = new BlurMaskFilter(2 * options.density, BlurMaskFilter.Blur.NORMAL);
    private void drawEllipses(@NonNull Plot plot) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            paint.setStyle(Paint.Style.FILL);
            paint.setMaskFilter(blurry);
            // Draw canopy ellipse
            paint.setColor(0x2244ee44);
            plot.canvas.save();
            plot.canvas.rotate(9, plot.getX(11), plot.getY(-5.5));
            plot.canvas.drawOval(plot.getX(1), plot.getY(-1), plot.getX(21), plot.getY(-10), paint);
            plot.canvas.restore();
            // Draw wingsuit ellipse
            paint.setColor(0x229e62f2);
            plot.canvas.save();
            plot.canvas.rotate(35, plot.getX(38), plot.getY(-21));
            plot.canvas.drawOval(plot.getX(20), plot.getY(-10), plot.getX(56), plot.getY(-32), paint);
            plot.canvas.restore();
            paint.setMaskFilter(null);
        }
    }

    // Always keep square aspect ratio
    @NonNull
    @Override
    public Bounds getBounds(@NonNull Bounds dataBounds, int axis) {
        bounds.set(dataBounds);
        AdjustBounds.clean(bounds, inner, outer);
        AdjustBounds.squareBounds(bounds, getWidth(), getHeight(), options.padding);
        return bounds;
    }

}
