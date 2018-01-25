package com.platypii.baseline.views;

import com.platypii.baseline.Services;
import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.location.Geo;
import com.platypii.baseline.location.LocationProvider;
import com.platypii.baseline.places.Place;
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

// TODO: On tap, prompt to set home location?

public class NavView extends View implements Subscriber<MLocation> {

    private final float density = getResources().getDisplayMetrics().density;
    private final Paint paint = new Paint();
    private final Path arrow = new Path();

    public NavView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint.setTextAlign(Paint.Align.CENTER);
        // Software layer required for path
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        // Initialize arrow polygon
        final float w2 = 0.056f; // The width of the arrow
        arrow.setLastPoint(-w2, -0.8f);
        arrow.lineTo(0, -1);
        arrow.lineTo(w2, -0.8f);
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        final int width = getWidth();
        final int height = getHeight();

        final float center_x = width / 2.0f;
        final float center_y = height / 2.0f;

        final MLocation currentLocation = Services.location.lastLoc;
        if (currentLocation != null) { // TODO: Check for freshness?
            // Nearest place
            final Place place = Services.places.nearestPlace.cached(currentLocation);
            if (place != null) {
                // Bearing from here to place
                final double absoluteBearing = Geo.bearing(currentLocation.latitude, currentLocation.longitude, place.lat, place.lng);
                // Bearing relative to flight path
                final double bearing = absoluteBearing - Services.location.bearing();

                // Distance to place
                final double distance = Geo.distance(currentLocation.latitude, currentLocation.longitude, place.lat, place.lng);

                // Draw location label
                paint.setColor(0xffaaaaaa);
                paint.setTextSize(18 * density);
                canvas.drawText(place.name, center_x, 18 * density, paint);
                // Draw distance label
                paint.setColor(0xffeeeeee);
                paint.setTextSize(28 * density);
                canvas.drawText(Convert.distanceShort(distance), center_x, center_y + 14 * density, paint);

                // Draw bearing circle
                paint.setStrokeWidth(2);
                paint.setStyle(Paint.Style.STROKE);
                final float radius = 140;
                canvas.drawCircle(center_x, center_y, radius, paint);
                if (!Double.isNaN(bearing)) {
                    canvas.save();
                    canvas.translate(center_x, center_y);
                    canvas.rotate((float) bearing, 0, 0);
                    canvas.scale(radius, radius);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawPath(arrow, paint);
                    canvas.restore();
                }
            }
        } else {
            canvas.drawText("no signal", center_x, center_y, paint);
        }

//        // Hand
//        paint.setStyle(Paint.Style.FILL);
//        paint.setColor(0xff111111);
//        if(!Double.isNaN(altitude)) {
//            canvas.save();
//            final float theta360 = (float) (360 * altitude / options.max_altitude);
//            final float scale = (float) (radius * 0.90);
//            canvas.translate(center_x, center_y);
//            canvas.rotate(theta360, 0, 0);
//            canvas.scale(scale, scale);
//            canvas.drawPath(hand, paint);
//            canvas.restore();
//        }
    }

    public void start(@NonNull LocationProvider locationService, @NonNull MyAltimeter altimeter) {
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
