package com.platypii.baseline.views.charts;

import com.platypii.baseline.events.ChartFocusEvent;
import com.platypii.baseline.measurements.MLocation;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import java.util.Collections;
import org.greenrobot.eventbus.EventBus;

public class TimeChartTouchable extends TimeChart {

    public TimeChartTouchable(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            final long millis = (long) plot.getXinverse(0, event.getX());
            // Find nearest data point
            final MLocation closest = findClosest(millis);
            // Emit chart focus event
            EventBus.getDefault().post(new ChartFocusEvent(closest));
        }
        return true; // if the event was handled
    }

    // Avoid creating new object just to binary search
    private final MLocation touchLocation = new MLocation();
    /**
     * Performs a binary search for the nearest data point
     */
    @Nullable
    private MLocation findClosest(long millis) {
        if (trackData != null && !trackData.isEmpty()) {
            touchLocation.millis = millis;
            int closest_index = Collections.binarySearch(trackData, touchLocation);
            if (closest_index < 0) closest_index = -closest_index;
            if (closest_index == trackData.size()) closest_index--;
            return trackData.get(closest_index);
        } else {
            return null;
        }
    }

}
