package com.platypii.baseline.views;

import com.platypii.baseline.Services;
import com.platypii.baseline.views.charts.PolarPlot;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class PolarActivity extends BaseActivity {

    private PolarPlot polar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        polar = new PolarPlot(this, null);
        setContentView(polar);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start sensor updates
        polar.start(Services.location, Services.alti);
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Stop sensor updates
        polar.stop();
    }

}