package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.views.BaseActivity;
import com.platypii.baseline.views.charts.FlightProfile;
import com.platypii.baseline.views.charts.layers.ProfileLayer;
import android.os.Bundle;
import android.util.Log;
import com.google.firebase.analytics.FirebaseAnalytics;
import java.util.ArrayList;
import java.util.List;

public class LaserActivity extends BaseActivity {
    private static final String TAG = "LaserActivity";

    private FlightProfile chart;
    private LaserPanelFragment laserPanel;

    // List of tracks and laser profiles to display
    final List<ProfileLayer> layers = new ArrayList<>();

    // TODO: remove static instance after debugging
    public static FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laser);

        // Find views
        chart = findViewById(R.id.flightProfile);

        // Load laser panel fragment
        laserPanel = new LaserPanelFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.laserPanel, laserPanel)
                .commit();

        // Update laser listing
        Services.cloud.lasers.listAsync(this, false);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    void addLayer(ProfileLayer layer) {
        if (layers.contains(layer)) {
            // Don't add duplicate layer
            return;
        }
        layers.add(layer);
        chart.addLayer(layer);
        chart.invalidate();
    }

    void removeLayer(ProfileLayer layer) {
        if (layers.remove(layer)) {
            chart.removeLayer(layer);
            chart.invalidate();
            laserPanel.updateLayers();
        } else {
            Log.e(TAG, "Remove called on unknown layer");
        }
    }

    void updateLayers() {
        chart.invalidate();
        laserPanel.updateLayers();
    }

}
