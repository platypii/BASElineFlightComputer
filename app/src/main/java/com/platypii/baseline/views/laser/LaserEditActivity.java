package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.views.BaseActivity;
import com.platypii.baseline.views.charts.FlightProfile;
import com.platypii.baseline.views.tracks.TrackListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LaserEditActivity extends BaseActivity {
    private static final String TAG = "LaserEdit";

    private FlightProfile flightProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laser_edit);

        // Find views
        flightProfile = findViewById(R.id.flightProfile);
        findViewById(R.id.laserSave).setOnClickListener(this::laserSave);
        findViewById(R.id.laserCancel).setOnClickListener(this::laserCancel);
    }

    private void laserSave(View view) {
        firebaseAnalytics.logEvent("click_laser_edit_save", null);
        // TODO: Save laser
        finish();
    }

    private void laserCancel(View view) {
        firebaseAnalytics.logEvent("click_laser_edit_cancel", null);
        finish();
    }

}
