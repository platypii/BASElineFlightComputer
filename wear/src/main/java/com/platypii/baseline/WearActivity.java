package com.platypii.baseline;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.platypii.baseline.alti.AltimeterActivity;
import com.platypii.baseline.events.DataSyncEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class WearActivity extends Activity {
    private static final String TAG = "WearActivity";

    private TextView remoteStatus;
    private View remoteControls;
    private ImageButton recordButton;
    private ImageButton audibleButton;
    private ImageButton baselineButton;
    private ImageView signalStatus;

    private RemoteApp remoteApp;

    // Periodic update thread
    private boolean updating = false;
    private final int updateInterval = 250; // milliseconds
    private final Handler handler = new Handler();
    private final Runnable updateRunnable = new Runnable() {
        public void run() {
            if(updating) {
                updateUIState();
                handler.postDelayed(this, updateInterval);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);

        // Start wear messaging service
        remoteApp = new RemoteApp(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        updateUIState();

        // Find views
        findViews();

        // Start signal updates
        remoteApp.startPinging();
        updating = true;
        handler.postDelayed(updateRunnable, updateInterval);
    }

    private void findViews() {
        remoteStatus = (TextView) findViewById(R.id.remoteStatus);
        remoteControls = findViewById(R.id.remoteControls);
        baselineButton = (ImageButton) findViewById(R.id.baselineButton);
        recordButton = (ImageButton) findViewById(R.id.recordButton);
        audibleButton = (ImageButton) findViewById(R.id.audibleButton);
        signalStatus = (ImageView) findViewById(R.id.signalStatus);
    }

    public void clickRecord(View v) {
        if(remoteApp.logging) {
            Log.i(TAG, "Clicked stop");
            remoteApp.clickStop();
        } else {
            Log.i(TAG, "Clicked record");
            remoteApp.clickRecord();
        }
        updateUIState();
    }

    public void clickAudible(View v) {
        if(remoteApp.audible) {
            Log.i(TAG, "Clicked audible off");
            remoteApp.disableAudible();
        } else {
            Log.i(TAG, "Clicked audible on");
            remoteApp.enableAudible();
        }
        updateUIState();
    }

    public void clickAltimeter(View v) {
        Log.i(TAG, "Clicked altimeter");
        // Launch micro altimeter
        startActivity(new Intent(this, AltimeterActivity.class));
    }

    public void clickApp(View v) {
        Log.i(TAG, "Clicked wingsuit app");
        // Launch app
        remoteApp.startApp();
        remoteApp.requestDataSync();
    }

    /**
     * Update button states and clock
     */
    private void updateUIState() {
        // For some reason, findviewbyid often returns null on wear
        if(remoteStatus == null) {
            findViews();
            if(remoteStatus == null) {
                return;
            }
        }

        // Update baseline button
        if(remoteApp.isActive()) {
            baselineButton.setAlpha(1f);
            remoteStatus.setVisibility(View.GONE);
            remoteControls.setVisibility(View.VISIBLE);
            // Update remote control buttons
            if(remoteApp.isActive() && remoteApp.synced) {
                recordButton.setAlpha(1f);
                audibleButton.setAlpha(1f);
            } else {
                recordButton.setAlpha(0.4f);
                audibleButton.setAlpha(0.4f);
            }
            // Update logging button
            if (remoteApp.logging) {
                recordButton.setImageResource(R.drawable.square);
            } else {
                recordButton.setImageResource(R.drawable.circle);
            }
            // Update audible button
            if (remoteApp.audible) {
                audibleButton.setImageResource(R.drawable.audio_on);
            } else {
                audibleButton.setImageResource(R.drawable.audio);
            }
            // Update gps status
            if (remoteApp.locationStatus != null) {
                signalStatus.setImageResource(remoteApp.locationStatus.icon());
                signalStatus.setVisibility(View.VISIBLE);
            } else {
                signalStatus.setVisibility(View.GONE);
            }
        } else {
            baselineButton.setAlpha(0.4f);
            remoteStatus.setVisibility(View.VISIBLE);
            remoteStatus.setText(R.string.error_not_connected);
            remoteControls.setVisibility(View.GONE);
        }
    }

    // Listen for app state changes
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDataSyncEvent(DataSyncEvent event) {
        updateUIState();
    }

    @Override
    public void onStop() {
        super.onStop();
        remoteApp.stopPinging();
        EventBus.getDefault().unregister(this);
        updating = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        remoteApp.stopService();
        remoteApp = null;
    }

}
