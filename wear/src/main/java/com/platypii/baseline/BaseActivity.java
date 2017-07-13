package com.platypii.baseline;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public abstract class BaseActivity extends FragmentActivity {

    /* Request codes used to invoke user interactions */
    static final int RC_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize early services
        Services.create(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Start flight services
        // Log.d(TAG, getClass().getSimpleName() + " starting, starting services");
        Services.start(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // If track is still recording, services will wait
        // Log.d(TAG, getClass().getSimpleName() + " stopping, stopping services");
        Services.stop();
    }

}
