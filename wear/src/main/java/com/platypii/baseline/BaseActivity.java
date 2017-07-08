package com.platypii.baseline;

import android.support.v4.app.FragmentActivity;

public abstract class BaseActivity extends FragmentActivity {
    private static final String TAG = "BaseActivity";

    @Override
    protected void onStart() {
        super.onStart();

        // Start flight services
        // Log.d(TAG, getClass().getSimpleName() + " starting, starting services");
        Services.start(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        // If track is still recording, services will wait
        // Log.d(TAG, getClass().getSimpleName() + " stopping, stopping services");
        Services.stop();
    }

}
