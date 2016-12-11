package com.platypii.baseline;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import com.platypii.baseline.alti.AltimeterActivity;

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
        // Launch micro altimeter
        startActivity(new Intent(this, AltimeterActivity.class));
    }

    /**
     * Update button states and clock
     */
    private void updateUIState() {
        if(recordButton == null) {
            recordButton = (ImageButton) findViewById(R.id.recordButton);
        }
        if(audibleButton == null) {
            audibleButton = (ImageButton) findViewById(R.id.audibleButton);
        }

        if(recordButton != null) {
            if (recording) {
                recordButton.setImageResource(R.drawable.square);
            } else {
                recordButton.setImageResource(R.drawable.circle);
            }
        }
        if(audibleButton != null) {
            if (audible) {
                audibleButton.setImageResource(R.drawable.audio_on);
            } else {
                audibleButton.setImageResource(R.drawable.audio);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        wear.stop();
        wear = null;
    }

}
