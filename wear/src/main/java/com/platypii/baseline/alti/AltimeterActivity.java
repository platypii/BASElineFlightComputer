package com.platypii.baseline.alti;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.measurements.MAltitude;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AltimeterActivity extends Activity {

    private AnalogAltimeter analogAltimeter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_altimeter);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        analogAltimeter = (AnalogAltimeter) findViewById(R.id.analogAltimeter);
    }

    private void update() {
        analogAltimeter.setAltitude(Services.alti.altitudeAGL());
    }

    /**
     * Listen for altitude updates
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAltitudeEvent(MAltitude alt) {
        update();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Start sensor updates
        EventBus.getDefault().register(this);
        // Start services
        Services.start(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    @Override
    public void onStop() {
        super.onStop();
        // Stop sensor updates
        EventBus.getDefault().unregister(this);
        // Stop services
        Services.stop();
    }
}
