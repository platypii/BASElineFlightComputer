package com.platypii.baseline.augmented;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.location.Location;
import android.util.AttributeSet;
import android.view.View;

/**
 * Render exits
 */
public class AugmentedView extends View {

    // Current phone orientation
    private float pitch;
    private float roll;
    private float yaw;

    private final Paint paint = new Paint();

    // TODO: Get from camera
    private static final float h_fov = 90;
    private static final float v_fov = 40;

    // Current location from GPS
    private Location currentLocation;
    private final Location tempLocation = new Location("l");

    public AugmentedView(Context context, AttributeSet attrs) {
        super(context, attrs);
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

        if(currentLocation != null) {
            // Draw test location
            tempLocation.setLatitude(47.61219);
            tempLocation.setLongitude(-122.34534);
            tempLocation.setAltitude(135);
            drawTower(canvas, tempLocation, "B");
        }
    }

    private void drawTower(Canvas canvas, Location tower, String name) {
        // Compute bearing from current location to object
        final double bearing = currentLocation.bearingTo(tower);
        final double distance = currentLocation.distanceTo(tower);
        final double height = tower.getAltitude() - currentLocation.getAltitude(); // meters

        final float dx = getX(canvas.getWidth(), bearing);
        final float dy = getY(canvas.getHeight(), distance, height);
        final float dy0 = getY(canvas.getHeight(), distance, 0);

        paint.setColor(0xffee1111);
        paint.setStrokeWidth(5);
        canvas.drawText(name, dx + 10, dy - 10, paint);
        canvas.drawCircle(dx, dy, 10.0f, paint);
        canvas.drawLine(dx, dy0, dx, dy, paint);
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
        return (float) ( (-canvasWidth / h_fov) * relativeBearing);
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
    private float getY(int height) {
        return (float) ( (-height / v_fov) * Math.toDegrees(pitch));
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

    public void updateOrientation(float[] orientation) {
        yaw = orientation[0];
        pitch = orientation[1];
        roll = orientation[2];
        invalidate();
    }

    public void updateLocation(Location location) {
        currentLocation = location;
        invalidate();
    }
}
