package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.cloud.LaserUpload;
import com.platypii.baseline.events.BluetoothEvent;
import com.platypii.baseline.laser.LaserMeasurement;
import com.platypii.baseline.laser.LaserProfile;
import com.platypii.baseline.laser.RangefinderService;
import com.platypii.baseline.views.BaseActivity;
import com.platypii.baseline.views.charts.FlightProfile;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.platypii.baseline.bluetooth.BluetoothState.BT_CONNECTED;

public class LaserEditActivity extends BaseActivity {
    private static final String TAG = "LaserEdit";

    private final RangefinderService rangefinder = new RangefinderService();

    private FlightProfile flightProfile;
    private EditText laserName;
    private EditText laserText;
    private TextView laserStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_laser_edit);

        // Find views
        flightProfile = findViewById(R.id.flightProfile);
        laserName = findViewById(R.id.laserName);
        laserText = findViewById(R.id.laserText);
        laserStatus = findViewById(R.id.laserStatus);
        findViewById(R.id.laserSave).setOnClickListener(this::laserSave);
        findViewById(R.id.laserCancel).setOnClickListener(this::laserCancel);
    }

    @Override
    protected void onStart() {
        super.onStart();
        rangefinder.start(this);
        EventBus.getDefault().register(this);
    }

    private void laserSave(View view) {
        firebaseAnalytics.logEvent("click_laser_edit_save", null);
        final List<LaserMeasurement> points = LaserMeasurement.parse(laserText.getText().toString());
        final String name = laserName.getText().toString();
        final LaserProfile laserProfile = new LaserProfile("", name, false, "app", points);
        new Thread(() -> {
            LaserUpload.post(LaserEditActivity.this, laserProfile);
            finish();
        }).start();
    }

    private void laserCancel(View view) {
        firebaseAnalytics.logEvent("click_laser_edit_cancel", null);
        finish();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLaserMeasure(LaserMeasurement meas) {
        // Parse lasers
        final List<LaserMeasurement> points = LaserMeasurement.parse(laserText.getText().toString());
        // Add measurement to laser points
        points.add(meas);
        // Sort by horiz
        Collections.sort(points, (l1, l2) -> Double.compare(l1.x, l2.x));
        // Update text and chart
        updateText(points);
        flightProfile.setLasers(points);
    }

    /**
     * Render list of laser measurements as text
     */
    private void updateText(List<LaserMeasurement> lasers) {
        final StringBuilder sb = new StringBuilder();
        for (LaserMeasurement laser : lasers) {
            sb.append(String.format(Locale.US, "%.1f, %.1f\n", laser.x, laser.y));
        }
        laserText.setText(sb);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothEvent(BluetoothEvent event) {
        updateRangefinder();
    }

    private void updateRangefinder() {
        if (rangefinder.getState() == BT_CONNECTED) {
            laserStatus.setText(R.string.bluetooth_status_connected);
            laserStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.status_green, 0, 0, 0);
        } else {
            laserStatus.setText(R.string.bluetooth_status_disconnected);
            laserStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.status_red, 0, 0, 0);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        rangefinder.stop();
        EventBus.getDefault().unregister(this);
    }
}
