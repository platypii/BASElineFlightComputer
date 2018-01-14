package com.platypii.baseline.views;

import com.platypii.baseline.Services;
import com.platypii.baseline.location.LandingZone;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Convert;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

// TODO: On tap, prompt to set home location?

public class NavView extends View {

    private final float density = getResources().getDisplayMetrics().density;
    private final Paint paint = new Paint();
    private final Path arrow = new Path();

    public NavView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Software layer required for hand path, and inner blur
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        // Initialize arrow polygon
        final float w1 = 0.025f; // The radius of the dot
        final float w2 = 0.056f; // The width of the arrow
        arrow.setLastPoint(-w1, 0);
        arrow.setLastPoint(-w2, -0.8f);
        arrow.lineTo(0, -1);
        arrow.lineTo(w2, -0.8f);
        arrow.lineTo(w1, 0);
        arrow.arcTo(new RectF(-w1, -w1, w1, w1), 0, 180);
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        final int width = getWidth();
        final int height = getHeight();

        final float center_x = width / 2.0f;
        final float center_y = height / 2.0f;

        // Draw distance label
        paint.setColor(0xffeeeeee);
        paint.setTextSize(28 * density);
        paint.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(getDistance(), center_x, center_y, paint);

        // Draw bearing circle
        canvas.drawCircle(center_x, center_y, 100, paint);
        final double bearing = getBearing();
        if (!Double.isNaN(bearing)) {
            // TODO: Draw direction triangle
            canvas.drawPath(arrow, paint);
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

    private String getDistance() {
        final MLocation lastLoc = Services.location.lastLoc;
        if (LandingZone.homeLoc != null && lastLoc != null) {
            final double distance = lastLoc.distanceTo(LandingZone.homeLoc);
            return Convert.distance(distance);
        } else {
            return "";
        }
    }

    private double getBearing() {
        final MLocation lastLoc = Services.location.lastLoc;
        if (LandingZone.homeLoc != null && lastLoc != null) {
            final double homeBearing = lastLoc.bearingTo(LandingZone.homeLoc);
            return homeBearing - lastLoc.bearing();
        } else {
            return Double.NaN;
        }
    }

}
