package com.platypii.baseline.augmented;

import com.platypii.baseline.measurements.MLocation;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.List;

/**
 * Render exits
 */
public class AugmentedView extends View {

    private final Paint paint = new Paint();

    // Current phone orientation
    private float pitch;
    private float roll;
    private float yaw;

    // TODO: Get from camera
    private static final float h_fov = 90;
    private static final float v_fov = 40;
    private final float density = getResources().getDisplayMetrics().density;

    // Current location from GPS
    private MLocation currentLocation;

    // Geo points to draw as a path
    private List<MLocation> points;

    public AugmentedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        paint.setTextSize(64);
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // translate 0,0 to center
        canvas.translate(canvas.getWidth() / 2, canvas.getHeight() / 2);
        // use roll for screen rotation
        canvas.rotate((float) -Math.toDegrees(roll));

        // Draw horizon line
        drawHorizon(canvas);
        // Draw cardinal directions
        drawHorizonPoint(canvas, 0, "N", 0xffdddddd);
        drawHorizonPoint(canvas, 90, "E", 0xffdddddd);
        drawHorizonPoint(canvas, 180, "S", 0xffdddddd);
        drawHorizonPoint(canvas, 270, "W", 0xffdddddd);

        if (currentLocation != null) {
            if (points != null) {
                paint.setColor(0xff5b00ff);
                paint.setStrokeWidth(3 * density);
                drawPath(canvas, points);
            }
        }
    }

    /**
     * Calculate the screen x value for a point with given bearing
     * @param canvasWidth the canvas width in pixels
     * @param targetBearing the bearing to the target in degrees
     * @return the screen-space x coordinate in pixels
     */
    private float getX(int canvasWidth, double targetBearing) {
        // Translate, but normalize for the FOV of the camera -- basically, pixels per degree, times degrees == pixels
        final double relativeBearing = (Math.toDegrees(yaw) - targetBearing + 540.0) % 360.0 - 180.0;
        if (-90 < relativeBearing && relativeBearing < 90) {
            return (float) ( (-canvasWidth / h_fov) * relativeBearing);
        } else {
            // Don't return x value for points behind us
            return Float.NaN;
        }
    }

    /**
     * Calculate the screen y value for a point with given distance and relative height
     * @param canvasHeight the canvas height in pixels
     * @param targetDistance the distance to the target in meters
     * @param targetHeight the height of the object relative to viewer's altitude
     * @return the screen-space y coordinate in pixels
     */
    private float getY(int canvasHeight, double targetDistance, double targetHeight) {
        // Target angle above horizon
        final double targetPitch = Math.asin(targetHeight / targetDistance);
        return (float) ( (-canvasHeight / v_fov) * Math.toDegrees(pitch + targetPitch));
    }
    /** Horizon y offset */
    private float getY(int canvasHeight) {
        return (float) ( (-canvasHeight / v_fov) * Math.toDegrees(pitch));
    }

    private void drawHorizon(Canvas canvas) {
        final float dy0 = getY(canvas.getHeight());
        paint.setColor(0xffdddddd);
        paint.setStrokeWidth(1);
        final float maxDimension = Math.max(canvas.getWidth(), canvas.getHeight());
        canvas.drawLine(-maxDimension, dy0, maxDimension, dy0, paint);
    }

    private void drawHorizonPoint(Canvas canvas, double bearing, String name, int color) {
        paint.setColor(color);
        final float dx = getX(canvas.getWidth(), bearing);
        final float dy = getY(canvas.getHeight());
        canvas.drawText(name, dx + 10, dy - 10, paint);
        canvas.drawCircle(dx, dy, 10.0f, paint);
    }

    private void drawPath(Canvas canvas, List<MLocation> points) {
        boolean first = true;
        float prevX = 0;
        float prevY = 0;

        for (MLocation point : points) {
            // Compute bearing from current location to object
            final double bearing = currentLocation.bearingTo(point);
            final double distance = currentLocation.distanceTo(point);
            final double height = point.altitude_gps - currentLocation.altitude_gps;
            final float x = getX(canvas.getWidth(), bearing);
            final float y = getY(canvas.getHeight(), distance, height);

            if (first) {
                first = false;
            } else {
                canvas.drawLine(prevX,prevY,x,y,paint);
            }
            prevX = x;
            prevY = y;
        }
    }

    public void updateOrientation(float[] orientation) {
        yaw = orientation[0];
        pitch = orientation[1];
        roll = orientation[2];
        invalidate();
    }

    public void updateLocation(MLocation location) {
        currentLocation = location;
        invalidate();
    }

    /** Display track geo data as a path */
    public void updateTrackData(List<MLocation> points) {
        this.points = points;
    }
}
