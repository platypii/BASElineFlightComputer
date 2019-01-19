package com.platypii.baseline.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.views.BaseActivity;
import android.os.Bundle;
import android.widget.TextView;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class RangefinderActivity extends BaseActivity {
    private static final String TAG = "RangefinderActivity";

    private final RangefinderService rangefinder = new RangefinderService();
    private TextView bluetoothLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rangefinder);

        bluetoothLog = findViewById(R.id.bluetoothLog);
        bluetoothLog.setText("x, y (m)\n");
    }

    @Override
    protected void onStart() {
        super.onStart();

        rangefinder.start(this);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        rangefinder.stop();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothEvent(LaserMeasurement meas) {
        final String row = String.format(Locale.US, "%.1f, %.1f\n", meas.horiz, meas.vert);
        bluetoothLog.append(row);
    }

}
