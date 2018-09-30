package com.platypii.baseline.views;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class NavActivity extends BaseActivity {

    private NavView nav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        nav = new NavView(this, null);
        setContentView(nav);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start sensor updates
        nav.start();
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Stop sensor updates
        nav.stop();
    }

}
