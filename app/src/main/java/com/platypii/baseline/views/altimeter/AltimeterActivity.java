package com.platypii.baseline.views.altimeter;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.views.BaseActivity;
import com.platypii.baseline.views.PolarPlot;
import com.platypii.baseline.measurements.MAltitude;
import android.os.Bundle;
import android.view.Window;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AltimeterActivity extends BaseActivity {

    private PolarPlot polar;
    private AnalogAltimeterSettable analogAltimeter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_altimeter);

        polar = findViewById(R.id.polar);
        analogAltimeter = findViewById(R.id.analogAltimeter);
        analogAltimeter.setOverlay(false);
        analogAltimeter.setAlti(Services.alti);
    }

    private void updateFlightStats() {
        analogAltimeter.setAltitude(Services.alti.altitudeAGL());
    }

    /**
     * Listen for altitude updates
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAltitudeEvent(MAltitude alt) {
        updateFlightStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start sensor updates
        EventBus.getDefault().register(this);
        polar.start(Services.location);
        updateFlightStats();
    }
    @Override
    protected void onPause() {
        super.onPause();
        // Stop sensor updates
        EventBus.getDefault().unregister(this);
        polar.stop();
    }
}
