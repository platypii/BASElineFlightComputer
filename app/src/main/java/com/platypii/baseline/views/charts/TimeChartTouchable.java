package com.platypii.baseline.views.charts;

import com.platypii.baseline.events.ChartFocusEvent;
import com.platypii.baseline.measurements.MLocation;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import java.util.Date;
import org.greenrobot.eventbus.EventBus;

public class TimeChartTouchable extends TimeChart {
    private static final String TAG = "TimeChartTouchable";

    public TimeChartTouchable(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            final long millis = (long) plot.getXinverse(0, event.getX());
            Log.d(TAG, "Touch x = " + new Date(millis));
            // Find nearest data point
            MLocation closest = null;
            for (MLocation location : trackData) {
                if (closest == null || Math.abs(closest.millis - millis) < Math.abs(location.millis - millis)) {
                    closest = location;
                }
            }
            // Emit chart focus event
            EventBus.getDefault().post(new ChartFocusEvent(closest));
        }
        return true; // if the event was handled
    }

    @Override
    public void drawData(@NonNull Plot plot) {
        super.drawData(plot);
        // TODO: Draw focus line
    }

}
