package com.platypii.baseline.views;

import com.platypii.baseline.R;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class SpeedActivity extends BaseActivity {

    private SpeedView speedView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        speedView = new SpeedView(this, null);
        setContentView(speedView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start sensor updates
        speedView.start();
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Stop sensor updates
        speedView.stop();
    }

}
