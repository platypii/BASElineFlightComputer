package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.laser.LaserProfile;
import com.platypii.baseline.views.BaseActivity;
import com.platypii.baseline.views.charts.FlightProfile;
import android.content.Intent;
import android.os.Bundle;

public class LaserActivity extends BaseActivity {

    private FlightProfile flightProfile;

    static final int TRACK_REQUEST = 64;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laser);

        // Find views
        flightProfile = findViewById(R.id.flightProfile);

        // Load laser panel fragment
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.laserPanel, new LaserPanelFragment())
                .commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        if (requestCode == TRACK_REQUEST) {
            if (resultCode == RESULT_OK) {
                // User picked a track
                final String trackId = resultIntent.getStringExtra("trackId");
                // TODO: Set track selection
            }
        }
    }

    void updateLaser(LaserProfile laser) {
        flightProfile.setLasers(laser.points);
    }

}
