package com.platypii.baseline.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.views.BaseActivity;
import com.platypii.baseline.views.charts.FlightProfile;
import android.os.Bundle;
import android.widget.TextView;
import java.util.*;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class RangefinderActivity extends BaseActivity {

    private final RangefinderService rangefinder = new RangefinderService();

    private final List<LaserMeasurement> lasers = new ArrayList<>();

    // Views
    private FlightProfile chart;
    private TextView laserText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rangefinder);

        laserText = findViewById(R.id.laserText);
        updateText();

        chart = findViewById(R.id.flightProfile);
    }

    @Override
    protected void onStart() {
        super.onStart();

        rangefinder.start(this);
        EventBus.getDefault().register(this);
    }

    private void updateText() {
        final StringBuilder sb = new StringBuilder();
        sb.append("x, y (m)\n");
        for (LaserMeasurement laser : lasers) {
            sb.append(String.format(Locale.US, "%.1f, %.1f\n", laser.horiz, laser.vert));
        }
        laserText.setText(sb);
    }

    @Override
    protected void onStop() {
        super.onStop();
        rangefinder.stop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothEvent(LaserMeasurement meas) {
        lasers.add(meas);
        // Sort by horiz
        Collections.sort(lasers, (l1, l2) -> Double.compare(l1.horiz, l2.horiz));
        updateText();
        chart.setLasers(lasers);
    }

}
