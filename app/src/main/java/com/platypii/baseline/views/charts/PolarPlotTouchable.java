package com.platypii.baseline.views.charts;

import com.platypii.baseline.events.ChartFocusEvent;
import com.platypii.baseline.measurements.MLocation;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import org.greenrobot.eventbus.EventBus;

/**
 * Adds focus touching to polar plot
 */
public class PolarPlotTouchable extends PolarPlot {

    public PolarPlotTouchable(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        super.onTouchEvent(event);
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
        return true; // if the event was handled
    }

    /**
     * Performs a binary search for the nearest data point
     */
    @Nullable
    private MLocation findClosest(double x, double y) {
        if (trackData != null && !trackData.isEmpty()) {
            MLocation closest = null;
            double closestDistance = Double.POSITIVE_INFINITY;
            for (MLocation loc : trackData) {
                final double dx = loc.groundSpeed() - x;
                final double dy = -loc.climb + y;
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

}
