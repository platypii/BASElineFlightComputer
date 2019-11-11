package com.platypii.baseline.views.altimeter;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.measurements.MAltitude;
import com.platypii.baseline.util.PubSub;
import com.platypii.baseline.views.BaseActivity;
import com.platypii.baseline.views.charts.SpeedChartLive;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.Nullable;

public class AltimeterActivity extends BaseActivity implements PubSub.Subscriber<MAltitude> {

    @Nullable
    private SpeedChartLive speedChart;
    @Nullable
    private AnalogAltimeterSettable analogAltimeter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_altimeter);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        speedChart = findViewById(R.id.speed_chart_live);
        analogAltimeter = findViewById(R.id.analogAltimeter);
        analogAltimeter.setOverlay(false);
        analogAltimeter.setAlti(Services.alti);
    }

    private void updateFlightStats() {
        if (analogAltimeter != null) {
            analogAltimeter.setAltitude(Services.alti.altitudeAGL());
        }
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
        if (speedChart != null) {
            speedChart.start(Services.location, Services.alti);
        }
        updateFlightStats();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Stop sensor updates
        Services.alti.altitudeEvents.unsubscribeMain(this);
        if (speedChart != null) {
            speedChart.stop();
        }
    }
}
