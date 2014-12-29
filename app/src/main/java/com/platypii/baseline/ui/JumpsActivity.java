package com.platypii.baseline.ui;

import com.platypii.baseline.R;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;


public class JumpsActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.jumps);
        getActionBar().hide();
    }
}





