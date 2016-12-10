package com.platypii.baseline;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

public class WearActivity extends Activity {
    private static final String TAG = "WearActivity";

    private ImageButton recordButton;
    private ImageButton audibleButton;

    private WearSlave wear;

    // TODO: Use synced data
    private boolean recording = false;
    private boolean audible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);

        // Find views
        recordButton = (ImageButton) findViewById(R.id.recordButton);
        audibleButton = (ImageButton) findViewById(R.id.audibleButton);

        // Start wear messaging service
        wear = new WearSlave(this);
    }

    public void clickRecord(View v) {
        if(recording) {
            Log.i(TAG, "Clicked stop");
            wear.clickStop();
            recording = false;
        } else {
            Log.i(TAG, "Clicked record");
            wear.clickRecord();
            recording = true;
        }
        updateUIState();
    }

    public void clickAudible(View v) {
        if(audible) {
            Log.i(TAG, "Clicked audible off");
            wear.disableAudible();
            audible = false;
        } else {
            Log.i(TAG, "Clicked audible on");
            wear.enableAudible();
            audible = true;
        }
        updateUIState();
    }

    public void clickAltimeter(View v) {
        Log.i(TAG, "Clicked altimeter");
        // TODO: Launch micro altimeter
    }

    /**
     * Update button states and clock
     */
    private void updateUIState() {
        if(recording) {
            recordButton.setImageResource(R.drawable.square);
        } else {
            recordButton.setImageResource(R.drawable.circle);
        }
        if(audible) {
            audibleButton.setImageResource(R.drawable.audio_on);
        } else {
            audibleButton.setImageResource(R.drawable.audio);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wear.stop();
        wear = null;
    }

}
