package com.platypii.baseline;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class PolarActivity extends BaseActivity {

    private PolarPlot polar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_polar);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        polar = findViewById(R.id.polar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start sensor updates
        polar.start(Services.location);
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Stop sensor updates
        polar.stop();
    }

}
