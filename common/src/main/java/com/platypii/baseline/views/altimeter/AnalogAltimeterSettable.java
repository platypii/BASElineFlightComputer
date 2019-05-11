package com.platypii.baseline.views.altimeter;

import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.common.R;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.util.Numbers;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * This class extends the analog altimeter, and adds altitude adjustment.
 * Long press twice to enter MODE_SET, and then drag up and down to adjust alti.
 * Long press again to save.
 */
public class AnalogAltimeterSettable extends AnalogAltimeter implements GestureDetector.OnGestureListener, View.OnTouchListener {
    private static final String TAG = "AnalogAltimeterSettable";

    @Nullable
    private MyAltimeter alti = null;

    // How long to wait in each mode
    private static final int MODE_PROMPT_TIMEOUT = 3000; // milliseconds
    private static final int MODE_SET_TIMEOUT = 5000; // milliseconds
    // Mode states
    private static final int MODE_ALTI = 0;
    private static final int MODE_PROMPT = 1;
    private static final int MODE_SET = 2;
    private int groundLevelMode = MODE_ALTI;

    // Ground level adjustment
    private double trueAltitude = 0; // Altitude AGL, without offset
    private double altitudeOffset = 0;

    // Gestures
    private final GestureDetector gestures;
    private final Handler handler = new Handler();
    private static final int updateInterval = 30; // milliseconds
    private float velocity = 0; // current swipe velocity

    public AnalogAltimeterSettable(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLongClickable(true);
        setOnTouchListener(this);
        gestures = new GestureDetector(getContext(), this);
    }

    /**
     * MUST be called to be able to set ground level
     */
    public void setAlti(@NonNull MyAltimeter alti) {
        this.alti = alti;
    }

    public void setGroundLevelMode(int mode) {
        this.groundLevelMode = mode;
        invalidate();
    }

    @Override
    public void setAltitude(double altitude) {
        trueAltitude = altitude;
        super.setAltitude(trueAltitude + altitudeOffset);
    }

    private void update() {
        super.setAltitude(trueAltitude + altitudeOffset);
    }

    @Override
    protected String getLabelText() {
        if (groundLevelMode == MODE_ALTI) {
            paint.setColor(0xffeeeeee);
        } else {
            paint.setColor(0xffee1111);
        }
        if (groundLevelMode == MODE_ALTI) {
            return super.getLabelText();
        } else if (groundLevelMode == MODE_PROMPT) {
            return getContext().getString(R.string.set_altitude);
        } else {
            final double altitude = trueAltitude + altitudeOffset;
            if (Numbers.isReal(altitude)) {
                return Convert.altitude(altitude);
            } else {
                return getContext().getString(R.string.no_barometer);
            }
        }
    }

    /**
     * Adjust altitude offset.
     * This applies a sigmoid function to make it slower near zero.
     */
    private void adjustOffset(double delta) {
        final double absAltitude = Math.abs(trueAltitude + altitudeOffset);
        altitudeOffset += delta * 0.6 * (Math.tanh(absAltitude * 0.002) + 0.05);
        update();
    }

    /** Listen for gestures */
    @Override
    public boolean onTouch(View view, MotionEvent event) {
        gestures.onTouchEvent(event);
        return false;
    }
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if (groundLevelMode == MODE_SET) {
            // Scroll (drag) gesture
            velocity = 0;
            adjustOffset(distanceY);
            // Reset timer
            handler.removeCallbacks(reaper);
            handler.postDelayed(reaper, MODE_SET_TIMEOUT);
        }
        return true;
    }
    @Override
    public boolean onDown(MotionEvent e) {
        velocity = 0;
        return true;
    }
    private static final int SWIPE_MIN_DISTANCE = 50;
    private static final int SWIPE_THRESHOLD_VELOCITY = 100;
    private static final float DECELERATION = 6;
    @Override
    public boolean onFling(@NonNull MotionEvent e1, @NonNull MotionEvent e2, float velocityX, final float velocityY) {
        if (groundLevelMode == MODE_SET && Math.abs(e1.getY() - e2.getY()) > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
            // Animate
            velocity = -velocityY / 80;
            handler.removeCallbacks(flinger);
            handler.post(flinger);
        }
        return true;
    }
    private final Runnable flinger = new Runnable() {
        public void run() {
            if (groundLevelMode == MODE_SET) {
                adjustOffset(velocity);
                if (Math.abs(DECELERATION) < Math.abs(velocity)) {
                    if (velocity < 0)
                        velocity += DECELERATION;
                    else
                        velocity -= DECELERATION;
                    handler.postDelayed(this, updateInterval);
                } else {
                    // Done flinging, reset timer
                    handler.removeCallbacks(reaper);
                    handler.postDelayed(reaper, MODE_SET_TIMEOUT);
                }
            }
        }
    };

    @Override
    public void onLongPress(MotionEvent e) {
        if (Double.isNaN(trueAltitude)) {
            return;
        }
        if (groundLevelMode == MODE_ALTI) {
            Log.i(TAG, "Ground level adjustment prompt");
            setGroundLevelMode(MODE_PROMPT);
            // Start a delayed thread to revert to alti mode
            handler.postDelayed(reaper, MODE_PROMPT_TIMEOUT);
        } else if (groundLevelMode == MODE_PROMPT) {
            Log.i(TAG, "Ground level adjustment start");
            setGroundLevelMode(MODE_SET);
            // Start a delayed thread to revert to alti mode
            handler.removeCallbacks(reaper);
            handler.postDelayed(reaper, MODE_SET_TIMEOUT);
        } else {
            Log.i(TAG, "Finished ground level adjustment: " + altitudeOffset + "m");
            handler.removeCallbacks(reaper);
            setGroundLevelMode(MODE_ALTI);
            // Save ground level adjustment
            if (alti != null) {
                alti.groundLevel.setCurrentAltitudeAGL(trueAltitude + altitudeOffset);
            } else {
                Exceptions.report(new IllegalStateException("AnalogAltimeterSettable requires call to setAlti()"));
            }
        }
        altitudeOffset = 0;
    }
    private final Runnable reaper = () -> {
        if (groundLevelMode != MODE_ALTI) {
            setGroundLevelMode(MODE_ALTI);
            altitudeOffset = 0;
            update();
        }
    };

    @Override
    public void onShowPress(MotionEvent e) {}
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }
}
