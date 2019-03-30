package com.platypii.baseline.views;

import com.platypii.baseline.Services;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.PubSub.Subscriber;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

public class SpeedView extends View implements Subscriber<MLocation> {

    private final float density = getResources().getDisplayMetrics().density;
    private final Paint paint = new Paint();
    private final Paint text = new Paint();
    private final Path triangle = new Path();

    public SpeedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(0xffbbbbbb);
        paint.setStrokeWidth(4);
        paint.setStyle(Paint.Style.STROKE);
        text.setColor(0xffeeeeee);
        // Software layer required for path
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        // Initialize triangle polygon
        triangle.setLastPoint(180, 40);
        triangle.lineTo(560, 300);
        triangle.lineTo(180, 300);
        triangle.close();
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        final int width = getWidth();
        final int height = getHeight();

        final float center_x = width * 0.5f;
        final float center_y = height * 0.5f;

        if (Services.location.isFresh()) {
            final MLocation loc = Services.location.lastLoc;
            text.setTextAlign(Paint.Align.LEFT);
            text.setTextSize(26 * density);
            // Draw horizontal speed
            canvas.drawText(Convert.speed(loc.groundSpeed(), 0, true), center_x, height, text);
            // Draw vertical speed
            if (loc.climb <= 0) {
                canvas.drawText(Convert.speed(-loc.climb, 0, true), 20, center_y, text);
            } else {
                canvas.drawText("+" + Convert.speed(loc.climb, 0, true), 20, center_y, text);
            }
            // Draw total speed
            canvas.drawText(Convert.speed(loc.totalSpeed(), 0, true), 400, 140, text);
            // Draw glide
            canvas.drawText(Convert.glide2(loc.groundSpeed(), loc.climb, 2, true), 400, 80, text);

            // Draw triangle
            canvas.drawPath(triangle, paint);
        } else {
            text.setTextAlign(Paint.Align.CENTER);
            text.setTextSize(24 * density);
            canvas.drawText("no signal", center_x, center_y, text);
        }
    }

    public void start() {
        // Start listening for location updates
        Services.location.locationUpdates.subscribe(this);
    }
    public void stop() {
        // Stop listening for location updates
        Services.location.locationUpdates.unsubscribe(this);
    }

    @Override
    public void apply(@NonNull MLocation loc) {
        postInvalidate();
    }

}
