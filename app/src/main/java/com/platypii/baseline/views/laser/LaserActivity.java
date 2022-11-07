package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.RequestCodes;
import com.platypii.baseline.Services;
import com.platypii.baseline.databinding.ActivityLaserBinding;
import com.platypii.baseline.events.ChartFocusEvent;
import com.platypii.baseline.events.ProfileLayerEvent;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.views.BaseActivity;
import com.platypii.baseline.views.charts.layers.ChartLayer;
import com.platypii.baseline.views.charts.layers.ProfileLayer;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

    private ActivityLaserBinding binding;

    // TODO: remove static instance after debugging
    public static FirebaseAnalytics firebaseAnalytics;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLaserBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
            binding.flightProfile.addLayer(layer);
        }
        EventBus.getDefault().register(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void addLayer(@NonNull ProfileLayerEvent.ProfileLayerAdded event) {
        Log.i(TAG, "Adding profile layer " + event.layer);
        binding.flightProfile.addLayer(event.layer);
        binding.flightProfile.invalidate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void removeLayer(@NonNull ProfileLayerEvent.ProfileLayerRemoved event) {
        Log.i(TAG, "Removing profile layer " + event.layer);
        binding.flightProfile.removeLayer(event.layer);
        binding.flightProfile.invalidate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateLayer(ProfileLayerEvent.ProfileLayerUpdated event) {
        binding.flightProfile.invalidate();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTrackFocus(@NonNull ChartFocusEvent.TrackFocused event) {
        binding.flightProfile.onChartFocus(event);
        if (!event.track.isEmpty()) {
            final MLocation start = event.track.get(0);
            final double x = start.distanceTo(event.location);
            final double y = event.location.altitude_gps - start.altitude_gps;
            binding.flightProfileStats.setText(String.format("%s x\n%s y", Convert.distance(x), Convert.distance(y)));
            binding.flightProfileStats.setVisibility(View.VISIBLE);
        } else {
            binding.flightProfileStats.setText("");
            binding.flightProfileStats.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLaserFocus(@NonNull ChartFocusEvent.LaserFocused event) {
        binding.flightProfile.onChartFocus(event);
        final double x = event.point.x;
        final double y = event.point.y;
        binding.flightProfileStats.setText(String.format("%s x\n%s y", Convert.distance(x), Convert.distance(y)));
        binding.flightProfileStats.setVisibility(View.VISIBLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUnFocus(@NonNull ChartFocusEvent.Unfocused event) {
        binding.flightProfile.onChartFocus(event);
        binding.flightProfileStats.setText("");
        binding.flightProfileStats.setVisibility(View.GONE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.RC_BLUE_ENABLE) {
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
        final List<ChartLayer> layers = new ArrayList<>(binding.flightProfile.getPlot().layers);
        for (ChartLayer layer : layers) {
            binding.flightProfile.removeLayer(layer);
        }
    }
}
