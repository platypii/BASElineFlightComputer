package com.platypii.baseline.views.altimeter;

import com.platypii.baseline.Services;
import com.platypii.baseline.databinding.ActivityAltimeterBinding;
import com.platypii.baseline.measurements.MAltitude;
import com.platypii.baseline.util.PubSub;
import com.platypii.baseline.views.BaseActivity;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class AltimeterActivity extends BaseActivity implements PubSub.Subscriber<MAltitude> {

    private ActivityAltimeterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        binding = ActivityAltimeterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.analogAltimeter.setAlti(Services.alti);
    }

    private void updateFlightStats() {
        binding.analogAltimeter.setAltitude(Services.alti.altitudeAGL());
    }

    /**
     * Listen for altitude updates
     */
    @Override
    public void apply(MAltitude alt) {
        updateFlightStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start sensor updates
        Services.alti.altitudeEvents.subscribeMain(this);
        binding.speedChartLive.start(Services.location, Services.alti);
        updateFlightStats();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop sensor updates
        Services.alti.altitudeEvents.unsubscribeMain(this);
        binding.speedChartLive.stop();
    }
}
