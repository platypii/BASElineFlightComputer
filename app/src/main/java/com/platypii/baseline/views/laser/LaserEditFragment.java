package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.events.RangefinderEvent;
import com.platypii.baseline.lasers.LaserMeasurement;
import com.platypii.baseline.lasers.LaserProfile;
import com.platypii.baseline.lasers.NewLaserForm;
import com.platypii.baseline.lasers.rangefinder.RangefinderService;
import com.platypii.baseline.location.Geocoder;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.measurements.LatLngAlt;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Analytics;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.views.charts.layers.LaserProfileLayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import static com.platypii.baseline.bluetooth.BluetoothState.BT_CONNECTED;

public class LaserEditFragment extends Fragment implements MyLocationListener {
    private static final String TAG = "LaserEditFrag";

    private final RangefinderService rangefinder = new RangefinderService();
    private static boolean rangefinderEnabled = false;

    private EditText laserName;
    private Spinner laserUnits;
    private EditText laserLocation;
    private EditText laserText;
    private TextView laserStatus;
    private ImageButton laserConnect;

    // Edit layer gets recreated on save, and the active one gets left in LaserLayers
    @NonNull
    private LaserProfile laserProfile = newLaserProfile();
    @NonNull
    private LaserProfileLayer editLayer = new LaserProfileLayer(laserProfile);

    @Nullable
    private MLocation defaultLocation = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.laser_edit, container, false);
        laserName = view.findViewById(R.id.laserName);
        laserUnits = view.findViewById(R.id.laserUnits);
        laserLocation = view.findViewById(R.id.laserLocation);
        laserText = view.findViewById(R.id.laserText);
        laserStatus = view.findViewById(R.id.laserStatus);
        laserConnect = view.findViewById(R.id.laserConnect);
        laserConnect.setOnClickListener(this::clickLaserConnect);
        view.findViewById(R.id.laserClear).setOnClickListener(this::clickClear);
        view.findViewById(R.id.laserSort).setOnClickListener(this::clickSort);
        view.findViewById(R.id.laserSave).setOnClickListener(this::laserSave);
        view.findViewById(R.id.laserCancel).setOnClickListener(this::laserCancel);

        // Set spinner to match default units
        if (!Convert.metric) {
            laserUnits.setSelection(1);
        }
        // Load saved draft
        loadForm();

        // Change listeners
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
        Services.lasers.layers.add(editLayer);
        Services.location.addListener(this);
        EventBus.getDefault().register(this);
        if (rangefinderEnabled) {
            final Activity activity = getActivity();
            if (activity != null) {
                rangefinder.start(activity);
            }
        }
        updateRangefinder();
    }

    // Update chart in parent activity
    private void updateLayers() {
        getLaserProfile();
        editLayer.loadLaser(laserProfile);
        Services.lasers.layers.update(editLayer);
    }

    /**
     * Generate a new laser profile with a temporary laser id.
     * Real laser id is returned later by the server.
     */
    @NonNull
    private LaserProfile newLaserProfile() {
        final String laserId = "tmp-" + UUID.randomUUID().toString();
        return new LaserProfile(laserId, null, "", false, 0.0, null, null, "app", new ArrayList<>());
    }

    /**
     * Load laser profile from form into LaserProfile
     */
    private void getLaserProfile() {
        laserProfile.name = laserName.getText().toString();
        laserProfile.user_id = AuthState.getUser();
        try {
            final LatLngAlt lla = Geocoder.parse(laserLocation.getText().toString());
            laserProfile.lat = lla.lat;
            laserProfile.lng = lla.lng;
            laserProfile.alt = lla.alt;
        } catch (ParseException e) {
            Log.w(TAG, "Failed to parse laser profile: " + e.getMessage());
            laserProfile.lat = null;
            laserProfile.lng = null;
            laserProfile.alt = 0.0;
        }
        laserProfile.points = LaserMeasurement.parseSafe(laserText.getText().toString(), isMetric());
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
        // Check for invalid location
        final String location = laserLocation.getText().toString().trim();
        if (!location.isEmpty()) {
            try {
                Geocoder.parse(location);
            } catch (ParseException e) {
                laserLocation.requestFocus();
                return e.getMessage();
            }
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
        Analytics.logEvent(getContext(), "click_laser_edit_save", null);
        final String error = validate();
        if (error == null) {
            // Save in background and return to profile list view
            getLaserProfile();
            // Publish laser as a new layer
            Services.lasers.addUnsynced(laserProfile);
            updateLayers();
            // Reset for next laser input
            laserProfile = newLaserProfile();
            editLayer = new LaserProfileLayer(laserProfile);
            clearForm();
            // Re-add edit layer since it will be removed on fragment stop
            Services.lasers.layers.add(editLayer);
            // Return to main fragment
            final FragmentManager fm = getFragmentManager();
            if (fm != null) fm.popBackStack();
        } else {
            // Form error
            Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Called when user clicks rangefinder button
     */
    private void clickLaserConnect(View view) {
        Analytics.logEvent(getContext(), "click_laser_edit_connect", null);
        rangefinderEnabled = true;
        final Activity activity = getActivity();
        if (activity != null) {
            rangefinder.start(activity);
        }
    }

    /**
     * Clear points button
     */
    private void clickClear(View view) {
        Analytics.logEvent(getContext(), "click_laser_edit_clear", null);
        laserText.setText("");
    }

    /**
     * Sort points button
     */
    private void clickSort(View view) {
        Analytics.logEvent(getContext(), "click_laser_edit_sort", null);
        // Sort by horizontal distance
        final boolean metric = isMetric();
        final List<LaserMeasurement> points = LaserMeasurement.parseSafe(laserText.getText().toString(), metric);
        Collections.sort(points, (l1, l2) -> Double.compare(l1.x, l2.x));
        // Update text box
        laserText.setText(LaserMeasurement.render(points, metric));
        // Update chart in parent activity
        updateLayers();
    }

    /**
     * Cancel laser: clear form and return
     */
    private void laserCancel(View view) {
        Analytics.logEvent(getContext(), "click_laser_edit_cancel", null);
        clearForm();
        final FragmentManager fm = getFragmentManager();
        if (fm != null) fm.popBackStack();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLaserMeasure(@NonNull LaserMeasurement meas) {
        // Insert newline if needed
        if (!laserText.getText().toString().endsWith("\n")) {
            laserText.append("\n");
        }
        final boolean metric = isMetric();
        final double units = metric ? 1 : 3.28084;
        // Append measurement to laser text
        final String line = String.format(Locale.US, "%.1f, %.1f\n", meas.x * units, meas.y * units);
        laserText.append(line);
        // Update chart in parent activity
        updateLayers();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothEvent(@NonNull RangefinderEvent event) {
        updateRangefinder();
    }

    private void updateRangefinder() {
        if (rangefinderEnabled) {
            laserConnect.setVisibility(View.GONE);
            if (rangefinder.getState() == BT_CONNECTED) {
                laserStatus.setText(R.string.rangefinder_connected);
                laserStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.status_green, 0, 0, 0);
            } else {
                laserStatus.setText(R.string.rangefinder_searching);
                laserStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.status_red, 0, 0, 0);
            }
            laserStatus.setVisibility(View.VISIBLE);
        } else {
            laserStatus.setVisibility(View.GONE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RangefinderService.ENABLE_BLUETOOTH_CODE) {
            // Notify rangefinder service that bluetooth was enabled
            rangefinder.bluetoothStarted(getActivity());
        }
    }

    private void loadForm() {
        final Context context = getContext();
        if (context != null) {
            final NewLaserForm form = NewLaserForm.load(context);
            laserName.setText(form.name);
            laserUnits.setSelection(form.metric ? 0 : 1);
            laserLocation.setText(form.latLngAlt);
            laserText.setText(form.points);
        }
    }

    private void saveForm() {
        final Context context = getContext();
        if (context != null) {
            new NewLaserForm(
                    laserName.getText().toString(),
                    isMetric(),
                    laserLocation.getText().toString(),
                    laserText.getText().toString()
            ).save(context);
        }
    }

    private void clearForm() {
        laserName.setText("");
        laserLocation.setText("");
        laserText.setText("");
    }

    @Override
    public void onLocationChanged(@NonNull MLocation loc) {
        if (defaultLocation == null) {
            defaultLocation = loc;
            final Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(() -> {
                    // Fill in lat,lon
                    laserLocation.setText(LatLngAlt.formatLatLngAlt(loc.latitude, loc.longitude, loc.altitude_gps));
                });
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        Services.location.removeListener(this);
        if (rangefinderEnabled) {
            rangefinder.stop();
        }
        Services.lasers.layers.remove(editLayer.id());
        saveForm();
    }
}
