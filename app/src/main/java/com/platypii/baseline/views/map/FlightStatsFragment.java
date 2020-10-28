package com.platypii.baseline.views.map;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.databinding.FlightStatsBinding;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.measurements.MAltitude;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Numbers;
import com.platypii.baseline.util.PubSub;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public class FlightStatsFragment extends Fragment implements MyLocationListener, PubSub.Subscriber<MAltitude> {

    private FlightStatsBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FlightStatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    private void update() {
        binding.flightStatsAlti.setText(Convert.altitude(Services.alti.altitude));
        if (Services.alti.climb < 0) {
            binding.flightStatsVario.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_downward_white_24dp, 0, 0, 0);
            binding.flightStatsVario.setText(Convert.speed(-Services.alti.climb));
        } else {
            binding.flightStatsVario.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_arrow_upward_white_24dp, 0, 0, 0);
            binding.flightStatsVario.setText(Convert.speed(Services.alti.climb));
        }
        final double groundSpeed = Services.location.groundSpeed();
        if (Numbers.isReal(groundSpeed)) {
            binding.flightStatsSpeed.setText(Convert.speed(groundSpeed));
            binding.flightStatsGlide.setText(Convert.glide(groundSpeed, Services.alti.climb, 2, true));
        } else {
            binding.flightStatsSpeed.setText("");
            binding.flightStatsGlide.setText("");
        }
    }

    private final Runnable updateRunnable = this::update;

    /**
     * Listen for altitude updates
     */
    @Override
    public void apply(MAltitude alt) {
        update();
    }

    @Override
    public void onLocationChanged(@NonNull MLocation loc) {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(updateRunnable);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Start sensor updates
        Services.location.addListener(this);
        Services.alti.altitudeEvents.subscribeMain(this);
        update();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Stop sensor updates
        Services.location.removeListener(this);
        Services.alti.altitudeEvents.unsubscribeMain(this);
    }

}
