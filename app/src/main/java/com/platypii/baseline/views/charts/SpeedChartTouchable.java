package com.platypii.baseline.views.charts;

import com.platypii.baseline.events.ChartFocusEvent;
import com.platypii.baseline.measurements.MLocation;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import org.greenrobot.eventbus.EventBus;

/**
 * Adds focus touching to speed chart
 */
public class SpeedChartTouchable extends SpeedChart {

    public SpeedChartTouchable(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        super.onTouchEvent(event);
        final int action = event.getAction();
        if (action == MotionEvent.ACTION_MOVE) {
            final double x = plot.getXinverse(0, event.getX());
            final double y = plot.getYinverse(0, event.getY());
            // Find nearest data point, and emit chart focus event
            EventBus.getDefault().post(findClosest(x, y));
        } else if (action == MotionEvent.ACTION_UP) {
            // Clear chart focus event
            EventBus.getDefault().post(new ChartFocusEvent.Unfocused());
        }
        return true; // if the event was handled
    }

    /**
     * Performs a search for the nearest data point
     */
    @NonNull
    private ChartFocusEvent findClosest(double x, double y) {
        ChartFocusEvent closest = new ChartFocusEvent.Unfocused();
        double closestDistance = Double.POSITIVE_INFINITY;
        if (trackData != null && !trackData.data.isEmpty()) {
            for (MLocation loc : trackData.data) {
                final double dx = loc.groundSpeed() - x;
                final double dy = -loc.climb + y;
                final double distance = dx * dx + dy * dy; // distance squared
                if (distance < closestDistance) {
                    closest = new ChartFocusEvent.TrackFocused(loc, trackData.data);
                    closestDistance = distance;
                }
            }
        }
        return closest;
    }
}
