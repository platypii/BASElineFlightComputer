package com.platypii.baseline.ui;

import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyAltitude;
import com.platypii.baseline.data.MyAltitudeListener;
import com.platypii.baseline.data.MyDatabase;
import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.BlurMaskFilter.Blur;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


public class AnalogAltimeter extends View {
    
    // Drawing options
    private static final double max_altitude = 12000 * Convert.FT; // TODO: not actually used

    // Avoid creating new objects unnecessarily
    private Paint paint = new Paint();
    private RectF circ = new RectF();
    private BlurMaskFilter blurMask;
    private static Path hand = new Path();
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
        this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        
        final float density = getResources().getDisplayMetrics().density;
        blurMask = new BlurMaskFilter(6 * density, Blur.INNER);
        paint.setAntiAlias(true);
        
        // Start altitude updates
        MyAltimeter.addListener(new MyAltitudeListener() {    
            public void doInBackground(MyAltitude alt) {}
            public void onPostExecute() {
                invalidate();
            }
        });
        
    }
    
    @Override
    public void onDraw(Canvas canvas) {

        final int width = getWidth();
        final int height = getHeight();
        final float density = getResources().getDisplayMetrics().density;
        
        final float center_x = width / 2.0f;
        final float center_y = height / 2.0f;
        
        final float radius = Math.min(center_x, center_y) - 5;
        final float inner_radius = radius * 0.65f;
        
        // Draw face
        paint.setColor(0xffdddddd);
        paint.setStyle(Paint.Style.FILL);
        paint.setMaskFilter(blurMask); // inset
        canvas.drawCircle(center_x, center_y, radius, paint);
        paint.setMaskFilter(null);
        
        // Draw alerts
        float breakoff_angle = (float) (360 * MyDatabase.events.breakoff_altitude / max_altitude);
        float deploy_angle =   (float) (360 * MyDatabase.events.deploy_altitude / max_altitude);
        float harddeck_angle = (float) (360 * MyDatabase.events.harddeck_altitude / max_altitude);
        // 2-color: 0xffff5533, 0xffdddd55
        // 3- color: 0xffff331f, 0xffff6633, 0xffdddd55
        paint.setStyle(Paint.Style.FILL);
        circ.set(center_x - inner_radius, center_y - inner_radius, center_x + inner_radius, center_y + inner_radius);
        // Breakoff
        paint.setColor(0xffdddd55);
        canvas.drawArc(circ, -90 + deploy_angle, breakoff_angle - deploy_angle, true, paint); // 2500..3000
        // Deploy
        paint.setColor(0xffee7733);
        canvas.drawArc(circ, -90 + harddeck_angle, deploy_angle - harddeck_angle, true, paint); // 0..2500
        // Harddeck
        paint.setColor(0xffee2211);
        canvas.drawArc(circ, -90, harddeck_angle, true, paint); // 0..2500
 
        // Labels
        paint.setColor(0xff000000);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(26 * density);
        paint.setTextAlign(Paint.Align.CENTER);
        for(int i = 0; i < 12; i++) {
            double theta = 2 * Math.PI * i / 12;
            double r = radius * 0.86;
            float x = (float)(center_x + r * Math.sin(theta));
            float y = (float)(center_y - r * Math.cos(theta)) + 9 * density;
            canvas.drawText(Integer.toString(i), x, y, paint);
        }
        paint.setTextSize(12 * density);
        canvas.drawText("x1000ft", center_x, center_y + radius * 0.3f, paint);
        
        // Draw lines
        paint.setColor(0xff111111);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(5 * density);
        canvas.drawCircle(center_x, center_y, inner_radius, paint);
        
        // Draw tick marks
        // Major
        paint.setStrokeWidth(5 * density);
        for(int i = 0; i < 12; i++) {
            double theta = 2 * Math.PI * i / 12;
            double r1 = radius * 0.60;
            double r2 = radius * 0.75;
            float x1 = (float)(center_x + r1 * Math.sin(theta));
            float y1 = (float)(center_y - r1 * Math.cos(theta));
            float x2 = (float)(center_x + r2 * Math.sin(theta));
            float y2 = (float)(center_y - r2 * Math.cos(theta));
            canvas.drawLine(x1, y1, x2, y2, paint);
        }
        
        // Minor
        paint.setStrokeWidth(3 * density);
        for(int i = 0; i < 12; i++) {
            double theta = 2 * Math.PI * (2 * i + 1) / 24.0;
            double r1 = radius * 0.65;
            double r2 = radius * 0.71;
            float x1 = (float)(center_x + r1 * Math.sin(theta));
            float y1 = (float)(center_y - r1 * Math.cos(theta));
            float x2 = (float)(center_x + r2 * Math.sin(theta));
            float y2 = (float)(center_y - r2 * Math.cos(theta));
            canvas.drawLine(x1, y1, x2, y2, paint);
        }
        
        // 1/2 Minor
        paint.setStrokeWidth(2 * density);
        for(int i = 0; i < 24; i++) {
            double theta = 2 * Math.PI * (2 * i + 1) / 48.0;
            double r1 = radius * 0.65;
            double r2 = radius * 0.68;
            float x1 = (float)(center_x + r1 * Math.sin(theta));
            float y1 = (float)(center_y - r1 * Math.cos(theta));
            float x2 = (float)(center_x + r2 * Math.sin(theta));
            float y2 = (float)(center_y - r2 * Math.cos(theta));
            canvas.drawLine(x1, y1, x2, y2, paint);
        }
        
        // Hand
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xff111111);
        if(!Double.isNaN(MyAltimeter.altitude)) {
            canvas.save();
            float theta360 = (float) (360 * MyAltimeter.altitude / max_altitude);
            float scale = (float) (radius * 0.90);
            canvas.translate(center_x, center_y);
            canvas.rotate(theta360, 0, 0);
            canvas.scale(scale, scale);
            canvas.drawPath(hand, paint);
            canvas.restore();
        } else {
            // Center dot
            canvas.drawCircle(center_x, center_y, 4 * density, paint);
        }
    }

}


