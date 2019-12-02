package com.platypii.baseline.views.charts;

import com.platypii.baseline.events.ChartFocusEvent;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Bounds;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import androidx.annotation.NonNull;
import java.util.Collections;
import org.greenrobot.eventbus.EventBus;

/**
 * Adds focus touching to time chart
 */
public class TimeChartTouchable extends TimeChart {
    private float lastX1 = 0;
    private float lastX2 = 0;
    private float scaleFactor = 1;
    private double startFactor = 0; // % of data to cut from start of track

    public TimeChartTouchable(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        super.onTouchEvent(event);

        final int action = event.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_POINTER_DOWN:
                // Save last touch points
                lastX1 = event.getX(0);
                lastX2 = event.getX(1);
                break;
            case MotionEvent.ACTION_UP:
                // Clear chart focus event
                EventBus.getDefault().post(new ChartFocusEvent.Unfocused());
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() == 1) {
                    final long millis = (long) plot.getXinverse(0, event.getX());
                    // Find nearest data point, and emit chart focus event
                    EventBus.getDefault().post(findClosest(millis));
                } else if (event.getPointerCount() == 2) {
                    float x1 = event.getX(0);
                    float x2 = event.getX(1);
                    // Compute gesture
                    final float scaleDelta = (x2 - x1) / (lastX2 - lastX1);
                    scaleFactor *= scaleDelta;
                    scaleFactor = Math.max(1f, Math.min(scaleFactor, 10f)); // Scale from 1x to 10x
                    final float b = (lastX2 * x1 - lastX1 * x2) / (lastX2 - lastX1);
                    startFactor -= b / getWidth() / scaleFactor;
                    startFactor = Math.max(0, Math.min(startFactor, 1 - 1 / scaleFactor));
                    // Save last touch points
                    lastX1 = x1;
                    lastX2 = x2;
                    this.invalidate();
                }
                break;
        }
        return true; // event was handled
    }


    // Avoid creating new object just to binary search
    private final MLocation touchLocation = new MLocation();

    /**
     * Performs a binary search for the nearest data point
     */
    @NonNull
    private ChartFocusEvent findClosest(long millis) {
        if (trackData != null && !trackData.isEmpty()) {
            touchLocation.millis = millis;
            int closest_index = Collections.binarySearch(trackData, touchLocation);
            if (closest_index < 0) closest_index = -closest_index - 1;
            if (closest_index == trackData.size()) closest_index--;
            return new ChartFocusEvent.TrackFocused(trackData.get(closest_index), trackData);
        } else {
            return new ChartFocusEvent.Unfocused();
        }
    }

    @NonNull
    @Override
    public Bounds getBounds(@NonNull Bounds dataBounds, int axis) {
        // Scale x range for all axes
        final double range = dataBounds.x.range();
        dataBounds.x.min = dataBounds.x.min + range * startFactor;
        dataBounds.x.max = dataBounds.x.min + range / scaleFactor;
        return super.getBounds(dataBounds, axis);
    }

}
