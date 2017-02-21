package com.platypii.baseline;

import android.os.Bundle;

public class TrackListActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jumps);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Start flight services
        Services.start(this);
    }
    @Override
    public void onStop() {
        super.onStop();
        // Stop flight services
        Services.stop();
    }
}
