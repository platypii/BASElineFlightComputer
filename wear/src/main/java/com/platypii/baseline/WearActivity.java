package com.platypii.baseline;

import com.platypii.baseline.location.LocationStatus;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

public class WearActivity extends BaseActivity {
    private static final String TAG = "WearActivity";

    private ImageView signalStatus;

    // Periodic UI updates
    private final Handler handler = new Handler();
    private final int signalUpdateInterval = 200; // milliseconds
    private Runnable signalRunnable = new Runnable() {
        public void run() {
            updateSignal();
            handler.postDelayed(this, signalUpdateInterval);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Start signal updates
        handler.post(signalRunnable);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop signal updates
        handler.removeCallbacks(signalRunnable);
    }

    /**
     * Update the views for GPS signal strength
     */
    private void updateSignal() {
        if(signalStatus == null) {
            signalStatus = findViewById(R.id.signalStatus);
        }
        if(signalStatus != null) {
            final LocationStatus status = LocationStatus.getStatus();
            signalStatus.setImageResource(status.icon);
        }
    }


    public void clickAltimeter(View v) {
        Log.i(TAG, "Clicked altimeter");
        // Launch micro altimeter
        startActivity(new Intent(this, AltimeterActivity.class));
    }

    public void clickPolar(View v) {
        Log.i(TAG, "Clicked polar");
        // Launch micro polar plot
        startActivity(new Intent(this, PolarActivity.class));
    }

    public void clickSettings(View v) {
        Log.i(TAG, "Clicked settings");
        // Launch settings activity
        startActivity(new Intent(this, SettingsActivity.class));
    }

}
