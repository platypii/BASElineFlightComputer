package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.cloud.lasers.LaserUploadTask;
import com.platypii.baseline.events.BluetoothEvent;
import com.platypii.baseline.laser.LaserLayers;
import com.platypii.baseline.laser.LaserMeasurement;
import com.platypii.baseline.laser.LaserProfile;
import com.platypii.baseline.laser.RangefinderService;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Numbers;
import com.platypii.baseline.views.charts.layers.LaserProfileLayer;
import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.firebase.analytics.FirebaseAnalytics;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.platypii.baseline.bluetooth.BluetoothState.BT_CONNECTED;
import static com.platypii.baseline.util.Numbers.parseDoubleNull;

public class LaserEditFragment extends Fragment implements MyLocationListener {

    private final FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

    private final RangefinderService rangefinder = new RangefinderService();

    private EditText laserName;
    private Spinner laserUnits;
    private EditText laserLat;
    private EditText laserLon;
    private EditText laserAlt;
    private EditText laserText;
    private TextView laserStatus;

    // Edit layer gets recreated on save, and the active one gets left in LaserLayers
    @NonNull
    private LaserProfileLayer editLayer = new LaserProfileLayer();

    @Nullable
    private MLocation defaultLocation = null;

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
        Services.location.addListener(this);
        EventBus.getDefault().register(this);
    }

    // Update chart in parent activity
    private void updateLayers() {
        editLayer.loadLaser(getLaserProfile());
        LaserLayers.getInstance().update(editLayer);
    }

    @NonNull
    private LaserProfile getLaserProfile() {
        final String name = laserName.getText().toString();
        final Double lat = parseDoubleNull(laserLat.getText().toString());
        final Double lng = parseDoubleNull(laserLon.getText().toString());
        final Double alt = parseDoubleNull(laserAlt.getText().toString());
        final List<LaserMeasurement> points = LaserMeasurement.parseSafe(laserText.getText().toString(), isMetric());
        return new LaserProfile("new-profile", AuthState.getUser(), name, false, alt, lat, lng, "app", points);
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
            laserName.requestFocus();
            return "Name cannot be empty";
        }
        final boolean metric = "meters".equals(laserUnits.toString());
        // Altitude is required
        if (laserAlt.getText().toString().isEmpty()) {
            laserName.requestFocus();
            return "Altitude needed for start performance";
        }
        // Validate points
        final String pointString = laserText.getText().toString();
        if (pointString.isEmpty()) {
            laserText.requestFocus();
            return "Measurements cannot be empty";
        }
        try {
            final int count = LaserMeasurement.parse(pointString, metric, true).size();
            if (count == 0) {
                laserText.requestFocus();
                return "Measurements cannot be empty";
            }
        } catch (ParseException e) {
            laserText.requestFocus();
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
            // Save in background and return to profile list view
            Services.tasks.add(new LaserUploadTask(laserProfile));
            // Add to cloud cache
            Services.cloud.lasers.cache.add(laserProfile);
            // Reset for next laser input
            editLayer = new LaserProfileLayer();
            // Re-add edit layer since it will be removed on fragment stop
            LaserLayers.getInstance().add(editLayer);
            // Return to main fragment
            final FragmentManager fm = getFragmentManager();
            if (fm != null) fm.popBackStack();
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
        final boolean metric = isMetric();
        final List<LaserMeasurement> points = LaserMeasurement.parseSafe(laserText.getText().toString(), metric);
        // Add measurement to laser points
        points.add(meas);
        // Sort by horiz
        Collections.sort(points, (l1, l2) -> Double.compare(l1.x, l2.x));
        // Update text box
        laserText.setText(LaserMeasurement.render(points, metric));
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
        Services.location.removeListener(this);
        rangefinder.stop();
        LaserLayers.getInstance().remove(editLayer);
    }

    @Override
    public void onLocationChanged(@NonNull MLocation loc) {
        if (defaultLocation == null) {
            defaultLocation = loc;
            final Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(() -> {
                    // Fill in lat,lon
                    laserLat.setText(Numbers.format6.format(loc.latitude));
                    laserLon.setText(Numbers.format6.format(loc.longitude));
                    laserAlt.setText(Numbers.format2.format(loc.altitude_gps));
                });
            }
        }
    }
}
