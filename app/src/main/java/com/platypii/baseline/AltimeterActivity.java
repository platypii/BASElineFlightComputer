package com.platypii.baseline;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.platypii.baseline.data.Convert;
import com.platypii.baseline.data.MyAltimeter;

public class AltimeterActivity extends Activity {

    // Periodic UI updates
    private static final int updateInterval = 80; // in milliseconds
    private final Handler handler = new Handler();
    private final Runnable updateRunnable = new Runnable() {
        public void run() {
            updateAlti();
            handler.postDelayed(this, updateInterval);
        }
    };

    // Views
    private TextView altitudeLabel;
    private TextView climbLabel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_altimeter);

        altitudeLabel = (TextView) findViewById(R.id.altitudeLabel);
        climbLabel = (TextView) findViewById(R.id.climbLabel);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Periodic UI updates
        handler.post(updateRunnable);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Stop UI updates
        handler.removeCallbacks(updateRunnable);
    }

    /**
     * Updates the altitude panel
     */
    public void updateAlti() {
        // Update views
        altitudeLabel.setText(Convert.distance(MyAltimeter.altitudeAGL()));
        climbLabel.setText(Convert.speed(MyAltimeter.climb));
    }
}
