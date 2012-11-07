package com.platypii.baseline.ui;

import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyAltitude;
import com.platypii.baseline.data.MyAltitudeListener;
import com.platypii.baseline.data.MyDatabase;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;


// TODO: 12000+
/**
 * Draws a thermometer-style altitude indicator
 * @author platypii
 */
public class LinearAltimeter extends View {

    // Drawing options
    private static final double max_altitude = 12000 * Convert.FT; // TODO: variable altimeter scale
    private static int margin = 2; // in dp
    private static final boolean outline = false;

    // Avoid creating new objects unnecessarily
    private Paint paint = new Paint();
    private Paint text = new Paint();
    private Paint textOutline = new Paint();
    private Path border = new Path();
    private Path mercury = new Path();
    private RectF rect = new RectF();
    
    
    // Initialize from XML
    public LinearAltimeter(Context context, AttributeSet attrs) {
        super(context, attrs);

        final float density = getResources().getDisplayMetrics().density;

        paint.setAntiAlias(true);
        text.setAntiAlias(true);
        text.setTypeface(Typeface.DEFAULT_BOLD);
        text.setTextAlign(Paint.Align.RIGHT);
        text.setTextSize(18 * density);
        textOutline.setStrokeWidth(1.5f * density);
        textOutline.setColor(0xeeeeeeee);
        textOutline.setStyle(Paint.Style.STROKE);
        
        // Start altitude updates
        MyAltimeter.addListener(new MyAltitudeListener() {    
            public void doInBackground(MyAltitude alt) {}
            public void onPostExecute() {
                invalidate();
            }
        });
    }
    
    @Override
    protected void onSizeChanged(int width, int height, int oldW, int oldH) {
        final float density = getResources().getDisplayMetrics().density;
        
        final int right = (int) (width - margin * density);
        int top = (int) (margin * density);
        int bottom = (int) (height - margin * density);

        final float radius1 = 15 * density; // Radius of border
        final float radius2 = 13 * density; // Smaller inner radius of mercury
        final int center_x = (int) (right - radius1);
        top += radius1;
        bottom -= radius1;
        
        // Border path
        border.reset();
        border.setLastPoint(center_x - radius1, bottom);
        border.lineTo(center_x - radius1, top);
        rect.set(center_x - radius1, top - radius1, center_x + radius1, top + radius1); // top border curve
        border.arcTo(rect, 180, 180);
        border.lineTo(center_x + radius1, bottom);
        rect.set(center_x - radius1, bottom - radius1, center_x + radius1, bottom + radius1); // bottom border curve
        border.arcTo(rect, 0, 180);
        
        // Mercury path (altitude height)
        mercury.reset();
        mercury.setLastPoint(center_x - radius2, bottom);
        mercury.lineTo(center_x - radius2, top);
        rect.set(center_x - radius2, top - radius2, center_x + radius2, top + radius2);
        mercury.arcTo(rect, 180, 180);
        mercury.lineTo(center_x + radius2, bottom);
        rect.set(center_x - radius2, bottom - radius2, center_x + radius2, bottom + radius2);
        mercury.arcTo(rect, 0, 180);
    
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        final float density = getResources().getDisplayMetrics().density;
    	final int width = canvas.getWidth();
        final int height = canvas.getHeight();
        
        final int right = (int) (width - margin * density);
        int top = (int) (margin * density);
        int bottom = (int) (height - margin * density);

        final float radius1 = 15 * density; // Radius of border
        final int center_x = (int) (right - radius1);
        final int text_x = (int) (center_x - radius1 - 3 * density);
        top += radius1;
        bottom -= radius1;

        // Draw background
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xff000000);
        canvas.drawPath(border, paint);
        
        // Draw alerts
        // 2-colors: 0xff770000, 0xffcc1111
        // 3-colors: 0xff660000, 0xff991111, 0xffee0000
        paint.setColor(0xff660000);
        canvas.save();
        canvas.clipRect(center_x - radius1, getY(bottom, top, MyDatabase.events.breakoff_altitude, max_altitude), center_x + radius1, bottom + radius1);
        canvas.drawPath(border, paint);
        canvas.restore();
        paint.setColor(0xff991111);
        canvas.save();
        canvas.clipRect(center_x - radius1, getY(bottom, top, MyDatabase.events.deploy_altitude, max_altitude), center_x + radius1, bottom + radius1);
        canvas.drawPath(border, paint);
        canvas.restore();
        paint.setColor(0xffee0000);
        canvas.save();
        canvas.clipRect(center_x - radius1, getY(bottom, top, MyDatabase.events.harddeck_altitude, max_altitude), center_x + radius1, bottom + radius1);
        canvas.drawPath(border, paint);
        canvas.restore();

        // Tick marks + labels
        paint.setColor(0xffbbbbbb);
        paint.setStrokeWidth(0);
        if(outline) {
            text.setColor(0xff111111);
            textOutline.set(text);
        } else {
            text.setColor(0xffdddddd);
        }
        for(int i = 0; i <= 12; i++) {
            int y = (int) getY(bottom, top, i * 1000 * Convert.FT, max_altitude);
            canvas.drawLine(center_x - 15 * density, y, center_x + 15 * density, y, paint);
            if(outline)
                canvas.drawText(Integer.toString(i), text_x, y + 6 * density, textOutline);
            canvas.drawText(Integer.toString(i), text_x, y + 6 * density, text);
        }
        
        // Altitude
        if(!Double.isNaN(MyAltimeter.altitude)) {
            float y = getY(bottom, top, MyAltimeter.altitude, max_altitude);
            // Draw altitude
            paint.setColor(0xffeeeeee);
            paint.setStrokeWidth(22 * density);
            canvas.save();
            canvas.clipRect(center_x - radius1, y, center_x + radius1, bottom + radius1);
            canvas.drawPath(mercury, paint);
            canvas.restore();
        }

        // Draw border
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.5f * density);
        paint.setColor(0xffdddddd);
        canvas.drawPath(border, paint);

    }
    
    private static float getY(int bottom, int top, double altitude, double max_altitude) {
        float percent = (float) (altitude / max_altitude);
        // percent = Math.max(0, Math.min(1, percent));
        return bottom - (bottom - top) * percent;
    }

}


