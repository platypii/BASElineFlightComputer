package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.cloud.LaserUpload;
import com.platypii.baseline.events.BluetoothEvent;
import com.platypii.baseline.laser.LaserMeasurement;
import com.platypii.baseline.laser.LaserProfile;
import com.platypii.baseline.laser.RangefinderService;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.analytics.FirebaseAnalytics;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.platypii.baseline.bluetooth.BluetoothState.BT_CONNECTED;

public class LaserEditFragment extends Fragment {
    private final FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());;

    private final RangefinderService rangefinder = new RangefinderService();

    private EditText laserName;
    private Spinner laserUnits;
    private EditText laserText;
    private TextView laserStatus;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.laser_edit_panel, container, false);
        laserName = view.findViewById(R.id.laserName);
        laserUnits = view.findViewById(R.id.laserUnits);
        laserText = view.findViewById(R.id.laserText);
        laserStatus = view.findViewById(R.id.laserStatus);
        view.findViewById(R.id.laserSave).setOnClickListener(this::laserSave);
        view.findViewById(R.id.laserCancel).setOnClickListener(this::laserCancel);
        laserText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                // Update chart in parent activity
                ((LaserActivity) getActivity()).updateLaser(getLaserProfile());
            }
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        rangefinder.start(getActivity());
        EventBus.getDefault().register(this);
    }

    private LaserProfile getLaserProfile() {
        final List<LaserMeasurement> points = LaserMeasurement.parseSafe(laserText.getText().toString(), isMetric());
        final String name = laserName.getText().toString();
        return new LaserProfile("", name, false, "app", points);
    }

    private boolean isMetric() {
        return "meters".equals(laserUnits.toString());
    }

    /**
     * Return true if the form is valid
     */
    private boolean validate() {
        // Validate name
        if (laserName.getText().toString().isEmpty()) {
            Toast.makeText(getContext(), "Name cannot be empty", Toast.LENGTH_LONG).show();
            return false;
        }
        final boolean metric = "meters".equals(laserUnits.toString());
        // Validate points
        final String pointString = laserText.getText().toString();
        if (pointString.isEmpty()) {
            Toast.makeText(getContext(), "Measurements cannot be empty", Toast.LENGTH_LONG).show();
            return false;
        }
        try {
            final int count = LaserMeasurement.parse(pointString, metric, true).size();
            if (count == 0) {
                Toast.makeText(getContext(), "Measurements cannot be empty", Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (ParseException e) {
            Toast.makeText(getContext(), "Invalid measurements, line " + e.getErrorOffset(), Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void laserSave(View view) {
        firebaseAnalytics.logEvent("click_laser_edit_save", null);
        if (validate()) {
            new Thread(() -> {
                final LaserProfile laserProfile = getLaserProfile();
                LaserUpload.post(getContext(), laserProfile);
                final FragmentManager fm = getFragmentManager();
                if (fm != null) fm.popBackStack();
            }).start();
        }
    }

    private void laserCancel(View view) {
        firebaseAnalytics.logEvent("click_laser_edit_cancel", null);
        final FragmentManager fm = getFragmentManager();
        if (fm != null) fm.popBackStack();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLaserMeasure(LaserMeasurement meas) {
        // Parse lasers
        final List<LaserMeasurement> points = LaserMeasurement.parseSafe(laserText.getText().toString(), isMetric());
        // Add measurement to laser points
        points.add(meas);
        // Sort by horiz
        Collections.sort(points, (l1, l2) -> Double.compare(l1.x, l2.x));
        // Update text box
        updateText(points);
        // Update chart in parent activity
        ((LaserActivity) getActivity()).updateLaser(getLaserProfile());
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
            laserStatus.setText("Rangefinder connected");
            laserStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.status_green, 0, 0, 0);
        } else {
            laserStatus.setText("Rangefinder not connected");
            laserStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.status_red, 0, 0, 0);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        rangefinder.stop();
        EventBus.getDefault().unregister(this);
    }
}
