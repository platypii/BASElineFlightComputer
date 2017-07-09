package com.platypii.baseline;

import com.platypii.baseline.altimeter.AnalogAltimeter;
import com.platypii.baseline.measurements.MAltitude;
import android.app.AlertDialog;
import android.os.Bundle;
import android.view.Window;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AltimeterActivity extends BaseActivity {

    private AlertDialog alertDialog;

    private PolarPlot polar;
    private AnalogAltimeter analogAltimeter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_altimeter);

        polar = (PolarPlot) findViewById(R.id.polar);
        analogAltimeter = (AnalogAltimeter) findViewById(R.id.analogAltimeter);
        analogAltimeter.setOverlay(false);
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
    public void onResume() {
        super.onResume();
        // Start sensor updates
        EventBus.getDefault().register(this);
        polar.start(Services.location);
        updateFlightStats();
    }
    @Override
    public void onPause() {
        super.onPause();
        // Stop sensor updates
        EventBus.getDefault().unregister(this);
        polar.stop();
        if(alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }
}
