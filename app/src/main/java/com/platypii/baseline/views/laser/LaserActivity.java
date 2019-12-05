package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.events.ChartFocusEvent;
import com.platypii.baseline.events.ProfileLayerEvent;
import com.platypii.baseline.lasers.rangefinder.RangefinderService;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.views.BaseActivity;
import com.platypii.baseline.views.charts.FlightProfile;
import com.platypii.baseline.views.charts.layers.ChartLayer;
import com.platypii.baseline.views.charts.layers.ProfileLayer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
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

    private FlightProfile flightProfile;
    private TextView flightProfileStats;

    // TODO: remove static instance after debugging
    public static FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laser);

        // Find views
        flightProfile = findViewById(R.id.flightProfile);
        flightProfileStats = findViewById(R.id.flightProfileStats);

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
        for (ProfileLayer layer : Services.lasers.layers.layers) {
            flightProfile.addLayer(layer);
        }
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void addLayer(@NonNull ProfileLayerEvent.ProfileLayerAdded event) {
        Log.i(TAG, "Adding profile layer " + event.layer);
        flightProfile.addLayer(event.layer);
        flightProfile.invalidate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void removeLayer(@NonNull ProfileLayerEvent.ProfileLayerRemoved event) {
        Log.i(TAG, "Removing profile layer " + event.layer);
        flightProfile.removeLayer(event.layer);
        flightProfile.invalidate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateLayer(ProfileLayerEvent.ProfileLayerUpdated event) {
        flightProfile.invalidate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTrackFocus(@NonNull ChartFocusEvent.TrackFocused event) {
        flightProfile.onChartFocus(event);
        if (!event.track.isEmpty()) {
            final MLocation start = event.track.get(0);
            final double x = start.distanceTo(event.location);
            final double y = event.location.altitude_gps - start.altitude_gps;
            final String yStr = y < 0 ? Convert.distance(-y) + "↓" : Convert.distance(y) + "↑";
            flightProfileStats.setText(String.format("%s →\n%s", Convert.distance(x), yStr));
            flightProfileStats.setVisibility(View.VISIBLE);
        } else {
            flightProfileStats.setText("");
            flightProfileStats.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLaserFocus(@NonNull ChartFocusEvent.LaserFocused event) {
        flightProfile.onChartFocus(event);
        final double x = event.point.x;
        final double y = event.point.y;
        final String yStr = y < 0 ? Convert.distance(-y) + "↓" : Convert.distance(y) + "↑";
        flightProfileStats.setText(String.format("%s →\n%s", Convert.distance(x), yStr));
        flightProfileStats.setVisibility(View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnFocus(@NonNull ChartFocusEvent.Unfocused event) {
        flightProfile.onChartFocus(event);
        flightProfileStats.setText("");
        flightProfileStats.setVisibility(View.GONE);
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
        final List<ChartLayer> layers = new ArrayList<>(flightProfile.getPlot().layers);
        for (ChartLayer layer : layers) {
            flightProfile.removeLayer(layer);
        }
    }
}
