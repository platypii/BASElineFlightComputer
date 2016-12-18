package com.platypii.baseline;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import com.platypii.baseline.alti.AltimeterActivity;
import com.platypii.baseline.events.DataSyncEvent;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class WearActivity extends Activity {
    private static final String TAG = "WearActivity";

    private ImageButton recordButton;
    private ImageButton audibleButton;
    private ImageButton baselineButton;

    private WearSlave wear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);

        // Start wear messaging service
        wear = new WearSlave(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        updateUIState();
    }

    public void clickRecord(View v) {
        if(wear.logging) {
            Log.i(TAG, "Clicked stop");
            wear.clickStop();
        } else {
            Log.i(TAG, "Clicked record");
            wear.clickRecord();
        }
        updateUIState();
    }

    public void clickAudible(View v) {
        if(wear.audible) {
            Log.i(TAG, "Clicked audible off");
            wear.disableAudible();
        } else {
            Log.i(TAG, "Clicked audible on");
            wear.enableAudible();
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
        wear.startApp();
        wear.requestDataSync();
    }

    /**
     * Update button states and clock
     */
    private void updateUIState() {
        if(baselineButton == null) {
            baselineButton = (ImageButton) findViewById(R.id.baselineButton);
        }
        if(recordButton == null) {
            recordButton = (ImageButton) findViewById(R.id.recordButton);
        }
        if(audibleButton == null) {
            audibleButton = (ImageButton) findViewById(R.id.audibleButton);
        }

        // Update baseline button
        if(baselineButton != null) {
            if(wear.synced) {
                baselineButton.setAlpha(1f);
            } else {
                baselineButton.setAlpha(0.5f);
            }
        }
        // Update record/stop button
        if(recordButton != null) {
            if(wear.synced) {
                recordButton.setAlpha(1f);
            } else {
                recordButton.setAlpha(0.5f);
            }
            if (wear.logging) {
                recordButton.setImageResource(R.drawable.square);
            } else {
                recordButton.setImageResource(R.drawable.circle);
            }
        }
        // Update audible button
        if(audibleButton != null) {
            if(wear.synced) {
                audibleButton.setAlpha(1f);
            } else {
                audibleButton.setAlpha(0.5f);
            }
            if (wear.audible) {
                audibleButton.setImageResource(R.drawable.audio_on);
            } else {
                audibleButton.setImageResource(R.drawable.audio);
            }
        }
    }

    // Listen for app state changes
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDataSyncEvent(DataSyncEvent event) {
        updateUIState();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wear.stop();
        wear = null;
    }

}
