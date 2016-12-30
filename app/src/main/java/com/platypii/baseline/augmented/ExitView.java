package com.platypii.baseline.augmented;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Render exits
 */
public class ExitView extends View {

    // Current phone orientation
    private float pitch;
    private float roll;
    private float yaw;
    // Bearing to target
    private static final float targetBearing = -90; // North

    private final Paint paint = new Paint();

    // TODO: Get from camera
    private static final float h_fov = 90;
    private static final float v_fov = 40;

    public ExitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setColor(Color.WHITE);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // use roll for screen rotation
        canvas.rotate((float) -Math.toDegrees(roll));
        // Translate, but normalize for the FOV of the camera -- basically, pixels per degree, times degrees == pixels
        final float dx = (float) ( (canvas.getWidth()/ h_fov) * (Math.toDegrees(yaw)-targetBearing));
        final float dy = (float) ( (canvas.getHeight()/ v_fov) * Math.toDegrees(pitch)) ;

        // wait to translate the dx so the horizon doesn't get pushed off
        canvas.translate(0.0f, 0.0f-dy);

        // make our line big enough to draw regardless of rotation and translation
        canvas.drawLine(0f - canvas.getHeight(), canvas.getHeight()/2, canvas.getWidth()+canvas.getHeight(), canvas.getHeight()/2, paint);

        // now translate the dx
        canvas.translate(0.0f-dx, 0.0f);

        // draw our point -- we've rotated and translated this to the right spot already
        canvas.drawCircle(canvas.getWidth()/2, canvas.getHeight()/2, 8.0f, paint);

        canvas.drawText("Hi", 20, 20, paint);
    }

    public void update(float[] orientation) {
        yaw = orientation[0];
        pitch = orientation[1];
        roll = orientation[2];
        invalidate();
    }
}
