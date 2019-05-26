package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.events.ProfileLayerEvent;
import com.platypii.baseline.laser.RangefinderService;
import com.platypii.baseline.views.BaseActivity;
import com.platypii.baseline.views.charts.FlightProfile;
import com.platypii.baseline.views.charts.layers.ProfileLayer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.firebase.analytics.FirebaseAnalytics;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class LaserActivity extends BaseActivity {
    private static final String TAG = "LaserActivity";

    private FlightProfile chart;
    // This is so we can keep track of profile layers in the chart
    private final List<ProfileLayer> layers = new ArrayList<>();

    // TODO: remove static instance after debugging
    public static FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laser);

        // Find views
        chart = findViewById(R.id.flightProfile);

        // Only add laser panel on first create
        if (savedInstanceState == null) {
            // Load laser panel fragment
            final LaserPanelFragment laserPanel = new LaserPanelFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.laserPanel, laserPanel)
                    .commit();
        }

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Add all layers
        for (ProfileLayer layer : Services.cloud.lasers.layers.layers) {
            chart.addLayer(layer);
            layers.add(layer);
        }
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void addLayer(@NonNull ProfileLayerEvent.ProfileLayerAdded event) {
        Log.i(TAG, "Adding profile layer " + event.layer);
        chart.addLayer(event.layer);
        chart.invalidate();
        layers.add(event.layer);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void removeLayer(@NonNull ProfileLayerEvent.ProfileLayerRemoved event) {
        Log.i(TAG, "Removing profile layer " + event.layer);
        chart.removeLayer(event.layer);
        chart.invalidate();
        layers.remove(event.layer);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateLayer(ProfileLayerEvent.ProfileLayerUpdated event) {
        chart.invalidate();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RangefinderService.ENABLE_BLUETOOTH_CODE) {
            // Send activity result to fragment
            final Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.laserPanel);
            if (fragment != null) {
                fragment.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        // Remove all chart profile layers
        for (ProfileLayer layer : layers) {
            chart.removeLayer(layer);
        }
        layers.clear();
    }
}
