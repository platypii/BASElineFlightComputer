package com.platypii.baseline;

import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import com.platypii.baseline.altimeter.AnalogAltimeterSettable;
import com.platypii.baseline.measurements.MAltitude;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AltimeterActivity extends BaseActivity {

    private AnalogAltimeterSettable analogAltimeter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_altimeter);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        analogAltimeter = findViewById(R.id.analogAltimeter);
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
    protected void onStart() {
        super.onStart();
        analogAltimeter.setAlti(Services.alti);
        // Start sensor updates
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Stop sensor updates
        EventBus.getDefault().unregister(this);
    }
}
