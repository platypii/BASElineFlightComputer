package com.platypii.baseline.alti;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class AnalogAltimeterSettable extends AnalogAltimeter implements GestureDetector.OnGestureListener, View.OnTouchListener {
    private static final String TAG = "AnalogAltimeterSettable";

    // How long to wait in each mode
    private static final int MODE_PROMPT_TIMEOUT = 3000; // milliseconds
    private static final int MODE_SET_TIMEOUT = 8000; // milliseconds
    // Mode states
    private static final int MODE_ALTI = 0;
    private static final int MODE_PROMPT = 1;
    private static final int MODE_SET = 2;
    private int groundLevelMode = MODE_ALTI;

    // Ground level adjustment
    private double altitudeOffset = 0;

    // Gestures
    private GestureDetector gestures;
    private final Handler handler = new Handler();
    private static final int updateInterval = 30; // milliseconds
    private float velocity = 0; // current swipe velocity

    public AnalogAltimeterSettable(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLongClickable(true);
        setOnTouchListener(this);
        gestures = new GestureDetector(getContext(), this);
    }

    public void setGroundLevelMode(int mode) {
        this.groundLevelMode = mode;
        invalidate();
    }

    @Override public void setAltitude(double altitude) {
        super.setAltitude(altitude + altitudeOffset);
    }

    private void update() {
        super.setAltitude(Services.alti.altitudeAGL() + altitudeOffset);
    }

    @Override protected String getLabelText() {
        if(groundLevelMode == MODE_ALTI) {
            paint.setColor(0xffeeeeee);
        } else {
            paint.setColor(0xffee1111);
        }
        if(groundLevelMode == MODE_PROMPT) {
            return getContext().getString(R.string.set_altitude);
        } else {
            if (Numbers.isReal(altitude)) {
                return Convert.altitude(altitude);
            } else {
                return getContext().getString(R.string.no_barometer);
            }
        }
    }

    /** Listen for gestures */
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        gestures.onTouchEvent(event);
        return false;
    }
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if(groundLevelMode == MODE_SET) {
            // Scroll (drag) gesture
            velocity = 0;
            final double delta = distanceY * Convert.FT;
            final double absAltitude = Math.abs(Services.alti.altitudeAGL() + altitudeOffset);
            if(absAltitude < 30) {
                // Slow down near zero
                altitudeOffset += delta / 12;
            } else if(absAltitude < 300) {
                altitudeOffset += delta / 5;
            } else if(absAltitude < 1000) {
                altitudeOffset += delta / 2;
            } else {
                altitudeOffset += delta;
            }
        }
        update();
        return true;
    }
    @Override
    public boolean onDown(MotionEvent e) {
        velocity = 0;
        return true;
    }
    private static final int SWIPE_MIN_DISTANCE = 50;
    private static final int SWIPE_THRESHOLD_VELOCITY = 100;
    private static final float DECELERATION = 4;
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, final float velocityY) {
        // Log.d(TAG, "onFling: " + e1 + " " + e2);
        if(Math.abs(e1.getY() - e2.getY()) > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
            // Animate
            velocity = -velocityY / 80;
            handler.removeCallbacks(flinger);
            handler.post(flinger);
        }
        return true;
    }
    private final Runnable flinger = new Runnable() {
        public void run() {
            if(groundLevelMode == MODE_SET) {
                altitudeOffset += velocity;
                update();
                if (Math.abs(DECELERATION) < Math.abs(velocity)) {
                    if (velocity < 0)
                        velocity += DECELERATION;
                    else
                        velocity -= DECELERATION;
                    handler.postDelayed(this, updateInterval);
                }
            }
        }
    };

    @Override
    public void onLongPress(MotionEvent e) {
        if(groundLevelMode == MODE_ALTI) {
            // Prompt to set ground level
            setGroundLevelMode(MODE_PROMPT);
            // Start a delayed thread to revert to alti mode
            handler.postDelayed(reaper, MODE_PROMPT_TIMEOUT);
        } else if(groundLevelMode == MODE_PROMPT) {
            Log.i(TAG, "Starting ground level adjustment");
            setGroundLevelMode(MODE_SET);
            // Start a delayed thread to revert to alti mode
            handler.removeCallbacks(reaper);
            handler.postDelayed(reaper, MODE_SET_TIMEOUT);
        } else {
            Log.i(TAG, "Finished ground level adjustment: " + altitudeOffset);
            handler.removeCallbacks(reaper);
            setGroundLevelMode(MODE_ALTI);
            // Save ground level adjustment
            Services.alti.setGroundLevel(Services.alti.ground_level - altitudeOffset);
        }
        altitudeOffset = 0;
    }
    private final Runnable reaper = new Runnable() {
        @Override
        public void run() {
            if(groundLevelMode != MODE_ALTI) {
                setGroundLevelMode(MODE_ALTI);
            }
        }
    };

    @Override
    public void onShowPress(MotionEvent e) {}
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }
}
