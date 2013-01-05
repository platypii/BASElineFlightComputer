package com.platypii.baseline.ui;

import java.util.ArrayList;

import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;


// A widget to display and set the ground level
public class GroundLevelWidget extends View implements OnGestureListener {

    private static final int margin = 0;
    private static int padding_top = 40;
    private static final int padding_bottom = 20;
    
    private static final double y_units = 1000 * Convert.FT;
    private static final double default_max_altitude = 304.8f; // Default maximum 1000ft
    
    public double offset = 0; // Altitude offset
    
    private ArrayList<GroundLevelWidgetListener> listeners = new ArrayList<GroundLevelWidgetListener>();

    // Avoid creating new objects unnecessarily
    private Paint paint = new Paint();
    private Paint text = new Paint();
    private Path path = new Path();
    private Bitmap jumper;
    
    // Gestures
    private GestureDetector gestures;
    private Handler handler = new Handler();
    private static final int updateInterval = 30; // in milliseconds
    private float velocity = 0; // current velocity;
    
    
    public GroundLevelWidget(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        padding_top = (int) (40 * getResources().getDisplayMetrics().density); // 40dp
        paint.setAntiAlias(true);
        text.setAntiAlias(true);
        text.setTextAlign(Align.RIGHT);
        text.setColor(0xff111111);
        gestures = new GestureDetector(this);

        jumper = BitmapFactory.decodeResource(getResources(), R.drawable.jumper);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        
        final int width = getWidth() - 2 * margin;
        final int height = getHeight() - 2 * margin;
        final float density = getResources().getDisplayMetrics().density;
        
        final int bottom = margin + height;
        final int top = margin;
        final int left = margin;
        final int right = margin + width;
        
        // Current altitude
        double altitude = MyAltimeter.altitude;
        if(Double.isNaN(altitude)) altitude = 0;
        altitude += offset;

        // x coordinates
        final float center_x = left  + width * 0.3f;
        final float span = 40; // the width of the talus

        // y coordinates
        final int zero = bottom - padding_bottom;
        final float max_y = (float)Math.max(default_max_altitude, altitude);
        final float y = getY(bottom, top, (float)altitude, max_y);
        final float y50 = getY(bottom, top, 40, max_y); // 50m
        final float y1000 = getY(bottom, top, 304.8f, max_y); // 1000ft 
        final float talus = Math.min(zero + (zero - y50), Math.max(y50, y)); // height of the top of the talus
        
        // Background
        final Shader sunset = new LinearGradient(left, zero, left, y1000, 0xffccddee, 0xff4488dd, Shader.TileMode.CLAMP);
        paint.setShader(sunset);
        canvas.drawRect(left, top, right, bottom, paint);
        paint.setShader(null);
        
        // Grid lines
        paint.setColor(0xff111111);
        paint.setStrokeWidth(0);
        text.setTextSize(14 * density);
        for(float meters = 0; meters <= max_y * 2; meters += y_units) { // TODO: Hack *2
            float grid_y = getY(bottom, top, meters, max_y);
            if(grid_y < top)
                break;
            canvas.drawLine(left, (int)grid_y, right, (int)grid_y, paint);
            canvas.drawText(Convert.distance(meters), right - 1, grid_y - 3, text);
        }
        
        // Draw ground
        paint.setColor(0xff000000);
        paint.setStyle(Style.FILL);
        path.rewind();
        path.setLastPoint(left, y);
        path.lineTo(center_x, y);
        path.lineTo(center_x, talus);
        path.quadTo(center_x, zero, center_x + span, zero);
        path.lineTo(right, zero);
        path.lineTo(right, bottom);
        path.lineTo(left, bottom);
        canvas.drawPath(path, paint);
        
        // Draw jumper
        float aspect = jumper.getWidth() / (float)jumper.getHeight();
        float jumper_height = zero - y50; // 50m tall jumper
        float jumper_width = aspect * jumper_height;
        RectF cliffEdge = new RectF(center_x - 5 - jumper_width, y + 2 - jumper_height, center_x - 5, y + 2);
        canvas.drawBitmap(jumper, null, cliffEdge, null); 
//      canvas.drawBitmap(bmp, center_x - 50, y - 134, null);
        
    }
    
    // Returns the screen-space y coordinate
    private float getY(int bottom, int top, float y, float max_y) {
        float height = bottom - top - padding_top - padding_bottom;
        return bottom - padding_bottom - height * y / max_y;
    }

    public void setOffset(double offset) {
        this.offset = offset;
        velocity = 0;
    }
    
    public void update() {
        this.postInvalidate();
        // Notify listeners
        for(GroundLevelWidgetListener listener : listeners) {
            listener.onGroundLevelChanged();
        }
    }
    
    public void onAltitudeChanged(double altitude) {
        update();
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestures.onTouchEvent(event);  
    }

    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        // Scroll (drag) gesture
        velocity = 0;
        offset += Convert.ft2m(distanceY); // 1 pixel = 1 foot
        update();
        return true;
    }
    public boolean onDown(MotionEvent e) {
        velocity = 0;
        return true;
    }
    private static final int SWIPE_MIN_DISTANCE = 50;
    private static final int SWIPE_THRESHOLD_VELOCITY = 100;
    private static final float DECELERATION = 3;
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, final float velocityY) {
        if(Math.abs(e1.getY() - e2.getY()) > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
            // Animate
            velocity = -velocityY / 80;
            handler.removeCallbacks(flinger);
            handler.post(flinger);
        }
        return true;
    }
    private Runnable flinger = new Runnable() {
        public void run() {
            offset += velocity;
            update();
            if(Math.abs(DECELERATION) < Math.abs(velocity)) {
                if(velocity < 0)
                    velocity += DECELERATION;
                else
                    velocity -= DECELERATION;
                handler.postDelayed(this, updateInterval);
            }
        }
    };
    public void onLongPress(MotionEvent e) {}
    public void onShowPress(MotionEvent e) {}
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    public void addListener(GroundLevelWidgetListener listener) {
        listeners.add(listener);
    }
    public interface GroundLevelWidgetListener {
        public void onGroundLevelChanged();
    }
    
}
