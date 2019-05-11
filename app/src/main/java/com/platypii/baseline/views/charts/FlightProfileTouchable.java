package com.platypii.baseline.views.charts;

import com.platypii.baseline.events.ChartFocusEvent;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Bounds;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import org.greenrobot.eventbus.EventBus;

public class FlightProfileTouchable extends FlightProfile {

    private float scaleFactor = 1;

    public FlightProfileTouchable(Context context, AttributeSet attrs) {
        super(context, attrs);

        final float density = getResources().getDisplayMetrics().density;
        options.padding.top = (int) (12 * density);
        options.padding.bottom = (int) (4 * density);
        options.padding.left = (int) (density);
        options.padding.right = (int) (4 * density);

        options.axis.x = options.axis.y = PlotOptions.axisDistance();
    }

    private final ScaleGestureDetector scaler = new ScaleGestureDetector(getContext(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scaleFactor *= detector.getScaleFactor();
            scaleFactor = Math.max(1f, Math.min(scaleFactor, 10f));
            invalidate();
            return true;
        }
    });
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final boolean superHandled = super.onTouchEvent(event);
        final boolean scalerHandled = scaler.onTouchEvent(event);

        // Focus
        if (!scaler.isInProgress()) {
            final int action = event.getAction();
            if (action == MotionEvent.ACTION_MOVE) {
                final double x = plot.getXinverse(0, event.getX());
                final double y = plot.getYinverse(0, event.getY());
                // Find nearest data point
                final MLocation closest = findClosest(x, y);
                // Emit chart focus event
                EventBus.getDefault().post(new ChartFocusEvent(closest));
            } else if (action == MotionEvent.ACTION_UP) {
                // Clear chart focus event
                EventBus.getDefault().post(new ChartFocusEvent(null));
            }
        }

        return true; // the event was handled
    }

    /**
     * Performs a search for the nearest data point
     */
    @Nullable
    private MLocation findClosest(double x, double y) {
        if (trackData != null && !trackData.isEmpty()) {
            final MLocation start = trackData.get(0);
            MLocation closest = null;
            double closestDistance = Double.POSITIVE_INFINITY;
            for (MLocation loc : trackData) {
                final double dx = start.distanceTo(loc) - x;
                final double dy = loc.altitude_gps - start.altitude_gps - y;
                final double distance = dx * dx + dy * dy; // distance squared
                if (distance < closestDistance) {
                    closest = loc;
                    closestDistance = distance;
                }
            }
            return closest;
        } else {
            return null;
        }
    }

    @NonNull
    @Override
    public Bounds getBounds(@NonNull Bounds dataBounds, int axis) {
        dataBounds.x.max /= scaleFactor;
        dataBounds.y.min /= scaleFactor;
        return super.getBounds(dataBounds, axis);
    }

}
