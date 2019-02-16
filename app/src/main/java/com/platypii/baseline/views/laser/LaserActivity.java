package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.events.ProfileLayerEvent;
import com.platypii.baseline.laser.LaserLayers;
import com.platypii.baseline.views.BaseActivity;
import com.platypii.baseline.views.charts.FlightProfile;
import com.platypii.baseline.views.charts.layers.ProfileLayer;
import android.os.Bundle;
import com.google.firebase.analytics.FirebaseAnalytics;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class LaserActivity extends BaseActivity {

    private FlightProfile chart;

    // TODO: remove static instance after debugging
    public static FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laser);

        // Find views
        chart = findViewById(R.id.flightProfile);

        // Load laser panel fragment
        final LaserPanelFragment laserPanel = new LaserPanelFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.laserPanel, laserPanel)
                .commit();

        // Update laser listing
        Services.cloud.lasers.listAsync(this, false);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Add all layers
        for (ProfileLayer layer : LaserLayers.getInstance().layers) {
            chart.addLayer(layer);
        }
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    void addLayer(ProfileLayerEvent.ProfileLayerAdded event) {
        chart.addLayer(event.layer);
        chart.invalidate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    void removeLayer(ProfileLayerEvent.ProfileLayerRemoved event) {
        chart.removeLayer(event.layer);
        chart.invalidate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    void updateLayer(ProfileLayerEvent.ProfileLayerUpdated event) {
        chart.invalidate();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
