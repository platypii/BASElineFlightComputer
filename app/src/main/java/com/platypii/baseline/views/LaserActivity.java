package com.platypii.baseline.views;

import com.platypii.baseline.R;
import com.platypii.baseline.views.charts.FlightProfile;
import com.platypii.baseline.views.tracks.TrackListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class LaserActivity extends BaseActivity {
    private static final String TAG = "Laser";

    private FlightProfile flightProfile;
    private int TRACK_REQUEST = 64;
    private int EXIT_REQUEST = 128;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laser);

        // Find views
        flightProfile = findViewById(R.id.flightProfile);
        findViewById(R.id.chooseTrack).setOnClickListener(this::chooseTrack);
        findViewById(R.id.chooseExit).setOnClickListener(this::chooseExit);
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

    private void chooseTrack(View view) {
        firebaseAnalytics.logEvent("click_profiles_choose_track", null);
        startActivityForResult(new Intent(this, TrackListActivity.class), TRACK_REQUEST);
    }

    private void chooseExit(View view) {
        firebaseAnalytics.logEvent("click_profiles_choose_exit", null);
        startActivityForResult(new Intent(this, LaserEditActivity.class), EXIT_REQUEST);
    }

}
