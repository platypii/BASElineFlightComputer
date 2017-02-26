package com.platypii.baseline.alti;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AltimeterActivity extends FragmentActivity implements GestureDetector.OnGestureListener {
    private static final String TAG = "Altimeter";

    private AnalogAltimeter analogAltimeter;

    // Altitude set mode
    private boolean groundLevelMode = false;
    private double altitudeOffset = 0;

    // Gestures
    private GestureDetector gestures;
    private final Handler handler = new Handler();
    private static final int updateInterval = 30; // milliseconds
    private float velocity = 0; // current swipe velocity

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_altimeter);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        analogAltimeter = (AnalogAltimeter) findViewById(R.id.analogAltimeter);
        analogAltimeter.setLongClickable(true);
        analogAltimeter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                toggleGroundLevelMode();
                return false;
            }
        });
        gestures = new GestureDetector(this, this);
    }

    private void update() {
        analogAltimeter.setAltitude(Services.alti.altitudeAGL() + altitudeOffset);
    }

    private void toggleGroundLevelMode() {
        groundLevelMode = !groundLevelMode;
        altitudeOffset = 0;
        if(groundLevelMode) {
            Log.i(TAG, "Starting ground level adjustment");
        } else {
            Log.i(TAG, "Finished ground level adjustment");
            // Save ground level adjustment
            Services.alti.setGroundLevel(Services.alti.ground_level - altitudeOffset);
        }
    }

    /**
     * Listen for altitude updates
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAltitudeEvent(MAltitude alt) {
        update();
    }

    /** Listen for gestures */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(TAG,"onTouchEvent: " + event);
        gestures.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        if(groundLevelMode) {
            // Scroll (drag) gesture
            velocity = 0;
            altitudeOffset += distanceY * Convert.FT; // 1 pixel = 1 foot
        }
        update();
        return true;
    }
    @Override
    public boolean onDown(MotionEvent e) {
        Log.d(TAG,"onDown: " + e);
        velocity = 0;
        return true;
    }
    private static final int SWIPE_MIN_DISTANCE = 50;
    private static final int SWIPE_THRESHOLD_VELOCITY = 100;
    private static final float DECELERATION = 3;
    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, final float velocityY) {
        Log.d(TAG, "onFling: " + e1 + " " +e2);
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
            if(groundLevelMode) {
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
    public void onLongPress(MotionEvent e) {}
    @Override
    public void onShowPress(MotionEvent e) {}
    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Start services
        Services.start(this);
        groundLevelMode = false;
        altitudeOffset = 0;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start sensor updates
        EventBus.getDefault().register(this);
        update();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop sensor updates
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        // Stop services
        Services.stop();
    }
}
