package com.platypii.baseline.views.altimeter;

import com.platypii.baseline.common.R;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Numbers;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

public class AnalogAltimeter extends View {

    private double altitude = 0.0;

    private final AnalogAltimeterOptions options;
    private boolean overlay = false;

    // Avoid creating new objects unnecessarily
    protected final Paint paint = new Paint();
    private final RectF circ = new RectF();
    private final BlurMaskFilter blurMask;
    private final RectF digitalAltiBox = new RectF();
    private final static Path hand = new Path();
    static {
        final float w1 = 0.025f; // The radius of the dot
        final float w2 = 0.056f; // The width of the arrow
        hand.setLastPoint(-w1, 0);
        hand.setLastPoint(-w2, -0.8f);
        hand.lineTo(0, -1);
        hand.lineTo(w2, -0.8f);
        hand.lineTo(w1, 0);
        hand.arcTo(new RectF(-w1, -w1, w1, w1), 0, 180);
    }

    public AnalogAltimeter(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Software layer required for hand path, and inner blur
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        if (Convert.metric) {
            options = AnalogAltimeterOptions.metric;
        } else {
            options = AnalogAltimeterOptions.imperial;
        }

        final float density = getResources().getDisplayMetrics().density;
        blurMask = new BlurMaskFilter(8 * density, Blur.INNER);
        paint.setAntiAlias(true);
    }

    public void setOverlay(boolean overlay) {
        this.overlay = overlay;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
        invalidate();
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        final int width = getWidth();
        final int height = getHeight();

        final float center_x = width / 2.0f;
        final float center_y = height / 2.0f;

        final float radius = Math.min(center_x, center_y) - 5;
        final float inner_radius = radius * 0.65f;

        // Adjust line thickness and text size based on size
        final float scale_factor = radius / 200;

        // Draw face
        if (overlay) {
            paint.setColor(0xccdddddd);
            paint.setStyle(Paint.Style.FILL);
            canvas.drawCircle(center_x, center_y, radius, paint);
        } else {
            paint.setColor(0xff000000);
            canvas.drawCircle(center_x, center_y, radius, paint);
            paint.setColor(0xffdddddd);
            paint.setStyle(Paint.Style.FILL);
            paint.setMaskFilter(blurMask); // inset
            canvas.drawCircle(center_x, center_y, radius, paint);
            paint.setMaskFilter(null);
        }

        // Draw alerts
        // 2-color: 0xffff5533, 0xffdddd55
        // 3-color: 0xffff331f, 0xffff6633, 0xffdddd55
        paint.setStyle(Paint.Style.FILL);
        circ.set(center_x - inner_radius, center_y - inner_radius, center_x + inner_radius, center_y + inner_radius);
        // Breakoff
        paint.setColor(0xffdddd55);
        canvas.drawArc(circ, -90 + options.deploy_angle, options.breakoff_angle - options.deploy_angle, true, paint); // 2500..3000
        // Deploy
        paint.setColor(0xffee7733);
        canvas.drawArc(circ, -90 + options.harddeck_angle, options.deploy_angle - options.harddeck_angle, true, paint); // 0..2500
        // Harddeck
        paint.setColor(0xffee2211);
        canvas.drawArc(circ, -90, options.harddeck_angle, true, paint); // 0..2500

        // Labels
        paint.setColor(0xff111111);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(32 * scale_factor);
        paint.setTextAlign(Paint.Align.CENTER);
        int major_i = 0;
        for (double theta = 0; theta < 2 * Math.PI; theta += options.major_angle) {
            double r = radius * 0.86;
            float x = (float)(center_x + r * Math.sin(theta));
            float y = (float)(center_y - r * Math.cos(theta)) + 9 * scale_factor;
            canvas.drawText(Integer.toString(major_i), x, y, paint);
            major_i++;
        }

        // Draw lines
        paint.setColor(0xff111111);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5 * scale_factor);
        canvas.drawCircle(center_x, center_y, inner_radius, paint);

        // Draw tick marks
        // Major
        paint.setStrokeWidth(5 * scale_factor);
        for (double theta = 0; theta < 2 * Math.PI; theta += options.major_angle) {
            final double r1 = radius * 0.60;
            final double r2 = radius * 0.75;
            final float x1 = (float)(center_x + r1 * Math.sin(theta));
            final float y1 = (float)(center_y - r1 * Math.cos(theta));
            final float x2 = (float)(center_x + r2 * Math.sin(theta));
            final float y2 = (float)(center_y - r2 * Math.cos(theta));
            canvas.drawLine(x1, y1, x2, y2, paint);
        }

        // Minor
        paint.setStrokeWidth(3 * scale_factor);
        for (double theta = options.major_angle / 2; theta < 2 * Math.PI; theta += options.major_angle) {
            final double r1 = radius * 0.65;
            final double r2 = radius * 0.71;
            final float x1 = (float)(center_x + r1 * Math.sin(theta));
            final float y1 = (float)(center_y - r1 * Math.cos(theta));
            final float x2 = (float)(center_x + r2 * Math.sin(theta));
            final float y2 = (float)(center_y - r2 * Math.cos(theta));
            canvas.drawLine(x1, y1, x2, y2, paint);
        }

        // 1/2 Minor
        paint.setStrokeWidth(2 * scale_factor);
        for (double theta = options.major_angle / 4; theta < 2 * Math.PI; theta += options.major_angle / 2) {
            final double r1 = radius * 0.65;
            final double r2 = radius * 0.68;
            final float x1 = (float)(center_x + r1 * Math.sin(theta));
            final float y1 = (float)(center_y - r1 * Math.cos(theta));
            final float x2 = (float)(center_x + r2 * Math.sin(theta));
            final float y2 = (float)(center_y - r2 * Math.cos(theta));
            canvas.drawLine(x1, y1, x2, y2, paint);
        }

        // Hand
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xff111111);
        if (!Double.isNaN(altitude)) {
            canvas.save();
            final float theta360 = (float) (360 * altitude / options.max_altitude);
            final float scale = (float) (radius * 0.90);
            canvas.translate(center_x, center_y);
            canvas.rotate(theta360, 0, 0);
            canvas.scale(scale, scale);
            canvas.drawPath(hand, paint);
            canvas.restore();
        } else {
            // Center dot
            canvas.drawCircle(center_x, center_y, 4 * scale_factor, paint);
        }

        // Draw digital alti background
        digitalAltiBox.set(
                center_x - 60 * scale_factor - 20, center_y - 20 * scale_factor - 12,
                center_x + 60 * scale_factor + 20, center_y + 20 * scale_factor + 12);
        paint.setColor(0xff333333);
        canvas.drawRoundRect(digitalAltiBox, 5 * scale_factor, 5 * scale_factor, paint);

        // Draw digital alti text
        paint.setColor(0xffeeeeee);
        paint.setTextSize(28 * scale_factor + 24);
        final String alt_label = getLabelText();
        canvas.drawText(alt_label, center_x, center_y + 12 * scale_factor + 4, paint);
    }

    /**
     * Override getLabelText to display text. Set paint for styles.
     */
    protected String getLabelText() {
        if (Numbers.isReal(altitude)) {
            return Convert.altitude(altitude);
        } else {
            return getContext().getString(R.string.no_barometer);
        }
    }

    @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int originalWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int originalHeight = MeasureSpec.getSize(heightMeasureSpec);
        int finalWidth, finalHeight;

        if (originalWidth > originalHeight) {
            finalWidth = originalHeight;
            finalHeight = originalHeight;
        } else {
            finalWidth = originalWidth;
            finalHeight = originalWidth;
        }

        super.onMeasure(
                MeasureSpec.makeMeasureSpec(finalWidth, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(finalHeight, MeasureSpec.EXACTLY));
    }
}
