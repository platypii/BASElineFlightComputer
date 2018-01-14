package com.platypii.baseline.views;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class NavActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final NavView navView = new NavView(this, null);
        setContentView(navView);
    }

}
