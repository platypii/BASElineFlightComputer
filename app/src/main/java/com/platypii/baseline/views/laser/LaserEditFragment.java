package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.lasers.LaserUpload;
import com.platypii.baseline.events.BluetoothEvent;
import com.platypii.baseline.laser.*;
import com.platypii.baseline.util.Numbers;
import com.platypii.baseline.views.charts.layers.LaserProfileLayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.google.firebase.analytics.FirebaseAnalytics;
import java.io.IOException;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.platypii.baseline.bluetooth.BluetoothState.BT_CONNECTED;

public class LaserEditFragment extends Fragment {
    private static final String TAG = "LaserEditFrag";

    private final FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());;

    private final RangefinderService rangefinder = new RangefinderService();

    private EditText laserName;
    private Spinner laserUnits;
    private EditText laserLat;
    private EditText laserLon;
    private EditText laserAlt;
    private EditText laserText;
    private TextView laserStatus;

    private LaserProfileLayer editLayer = new LaserProfileLayer();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.laser_edit, container, false);
        laserName = view.findViewById(R.id.laserName);
        laserUnits = view.findViewById(R.id.laserUnits);
        laserLat = view.findViewById(R.id.laserLat);
        laserLon = view.findViewById(R.id.laserLon);
        laserAlt = view.findViewById(R.id.laserAlt);
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
                updateLayers();
            }
        });
        laserUnits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateLayers();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        LaserLayers.getInstance().add(editLayer);
        rangefinder.start(getActivity());
        EventBus.getDefault().register(this);
    }

    // Update chart in parent activity
    private void updateLayers() {
        editLayer.loadLaser(getLaserProfile());
        LaserLayers.getInstance().update(editLayer);
    }

    private LaserProfile getLaserProfile() {
        final String name = laserName.getText().toString();
        final double lat = Numbers.parseDouble(laserLat.getText().toString());
        final double lon = Numbers.parseDouble(laserLon.getText().toString());
        final double alt = Numbers.parseDouble(laserAlt.getText().toString());
        final GeoPoint exit = new GeoPoint(System.currentTimeMillis(), alt, lat, lon);
        final List<LaserMeasurement> points = LaserMeasurement.parseSafe(laserText.getText().toString(), isMetric());
        return new LaserProfile("", name, false, exit, "app", points);
    }

    private boolean isMetric() {
        final int position = laserUnits.getSelectedItemPosition();
        final String[] values = getResources().getStringArray(R.array.metric_modes_values);
        final String value = values[position];
        return "meters".equals(value);
    }

    /**
     * Return null if the form is valid, or error message
     */
    @Nullable
    private String validate() {
        // Validate name
        if (laserName.getText().toString().isEmpty()) {
            return "Name cannot be empty";
        }
        final boolean metric = "meters".equals(laserUnits.toString());
        // Altitude is required
        if (laserAlt.getText().toString().isEmpty()) {
            return "Altitude needed for start performance";
        }
        // Validate points
        final String pointString = laserText.getText().toString();
        if (pointString.isEmpty()) {
            return "Measurements cannot be empty";
        }
        try {
            final int count = LaserMeasurement.parse(pointString, metric, true).size();
            if (count == 0) {
                return "Measurements cannot be empty";
            }
        } catch (ParseException e) {
            return "Invalid measurements, line " + e.getErrorOffset();
        }
        return null;
    }

    private void laserSave(View view) {
        firebaseAnalytics.logEvent("click_laser_edit_save", null);
        final String error = validate();
        if (error == null) {
            // Publish laser as a new layer
            final LaserProfile laserProfile = getLaserProfile();
            updateLayers();
            // Reset for next laser input
            editLayer = new LaserProfileLayer();
            // Post to server in background
            final Handler handler = new Handler();
            new Thread(() -> {
                try {
                    LaserUpload.post(getContext(), laserProfile);
                    // Return to main fragment
                    final FragmentManager fm = getFragmentManager();
                    if (fm != null) fm.popBackStack();
                } catch (IOException e) {
                    Log.e(TAG, "Failed to upload laser", e);
                    handler.post(() -> Toast.makeText(getContext(), "Profile upload failed", Toast.LENGTH_LONG).show());
                }
            }).start();
        } else {
            // Form error
            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
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
        laserText.setText(LaserMeasurement.render(points));
        // Update chart in parent activity
        updateLayers();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothEvent(BluetoothEvent event) {
        updateRangefinder();
    }

    private void updateRangefinder() {
        if (rangefinder.getState() == BT_CONNECTED) {
            laserStatus.setText(R.string.rangefinder_connected);
            laserStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.status_green, 0, 0, 0);
        } else {
            laserStatus.setText(R.string.rangefinder_searching);
            laserStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.status_red, 0, 0, 0);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        rangefinder.stop();
        LaserLayers.getInstance().remove(editLayer);
    }
}
