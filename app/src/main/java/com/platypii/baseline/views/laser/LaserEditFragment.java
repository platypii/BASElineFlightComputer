package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.RequestCodes;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.databinding.LaserEditBinding;
import com.platypii.baseline.events.RangefinderEvent;
import com.platypii.baseline.lasers.LaserMeasurement;
import com.platypii.baseline.lasers.LaserProfile;
import com.platypii.baseline.lasers.NewLaserForm;
import com.platypii.baseline.lasers.rangefinder.RangefinderService;
import com.platypii.baseline.location.Geocoder;
import com.platypii.baseline.Permissions;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.measurements.LatLngAlt;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Analytics;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.StringUtil;
import com.platypii.baseline.views.charts.layers.LaserProfileLayer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
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
import static com.platypii.baseline.bluetooth.BluetoothState.BT_CONNECTING;

public class LaserEditFragment extends Fragment implements MyLocationListener {
    private static final String TAG = "LaserEditFrag";

    @NonNull
    private final RangefinderService rangefinder = new RangefinderService();
    private static boolean rangefinderEnabled = false;

    private LaserEditBinding binding;

    // Edit layer gets recreated on save, and the active one gets left in LaserLayers
    @NonNull
    private LaserProfile laserProfile = newLaserProfile();
    @NonNull
    private LaserProfileLayer editLayer = new LaserProfileLayer(laserProfile, 0xffee1111, 2f);

    @Nullable
    private MLocation defaultLocation = null;

    @Nullable
    private String errorMessage = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = LaserEditBinding.inflate(inflater, container, false);

        binding.laserConnect.setOnClickListener(this::clickLaserConnect);
        binding.laserClear.setOnClickListener(this::clickClear);
        binding.laserSort.setOnClickListener(this::clickSort);
        binding.laserSave.setOnClickListener(this::laserSave);
        binding.laserCancel.setOnClickListener(this::laserCancel);

        // Set spinner to match default units
        if (!Convert.metric) {
            binding.laserUnits.setSelection(1);
        }
        // Load saved draft
        loadForm();

        // Change listeners
        binding.laserText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateLayers();
            }
        });
        binding.laserUnits.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateLayers();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        Services.lasers.layers.add(editLayer);
        Services.location.addListener(this);
        EventBus.getDefault().register(this);
        if (rangefinderEnabled) {
            laserConnect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
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
     * Load laser profile from form into laserProfile
     */
    private void getLaserProfile() {
        laserProfile.name = binding.laserName.getText().toString();
        laserProfile.user_id = AuthState.getUser();
        try {
            final LatLngAlt lla = Geocoder.parse(binding.laserLocation.getText().toString());
            laserProfile.lat = lla.lat;
            laserProfile.lng = lla.lng;
            laserProfile.alt = lla.alt;
        } catch (ParseException e) {
            Log.w(TAG, "Failed to parse laser profile location: " + e.getMessage());
            laserProfile.lat = null;
            laserProfile.lng = null;
            laserProfile.alt = 0.0;
        }
        laserProfile.points = LaserMeasurement.parseSafe(binding.laserText.getText().toString(), isMetric());
        final int quadrant = laserProfile.quadrant();
        if (quadrant == 0) {
            binding.laserWarning.setText(R.string.quadrant);
            binding.laserWarning.setVisibility(View.VISIBLE);
        } else if (quadrant == 1) {
            binding.laserWarning.setText(R.string.bottom);
            binding.laserWarning.setVisibility(View.VISIBLE);
        } else {
            binding.laserWarning.setVisibility(View.GONE);
        }
    }

    private boolean isMetric() {
        final int position = binding.laserUnits.getSelectedItemPosition();
        final String[] values = getResources().getStringArray(R.array.metric_modes_values);
        final String value = values[position];
        return "meters".equals(value);
    }

    /**
     * Return null if the form is valid, or error message
     */
    @Nullable
    private String validate() {
        // Clear highlights and remove excess lines
        final String pointString = binding.laserText.getText().toString().trim() + "\n";
        binding.laserText.setText(pointString);
        // Validate name
        if (binding.laserName.getText().toString().isEmpty()) {
            binding.laserName.requestFocus();
            return "Name cannot be empty";
        }
        final boolean metric = "meters".equals(binding.laserUnits.toString());
        // Check for invalid location
        final String location = binding.laserLocation.getText().toString().trim();
        if (!location.isEmpty()) {
            try {
                Geocoder.parse(location);
            } catch (ParseException e) {
                binding.laserLocation.requestFocus();
                return e.getMessage();
            }
        }
        // Validate points
        try {
            final int count = LaserMeasurement.parse(pointString, metric, true).size();
            if (count == 0) {
                binding.laserText.requestFocus();
                return "Measurements cannot be empty";
            }
        } catch (ParseException e) {
            binding.laserText.requestFocus();
            // Highlight line
            final int start = StringUtil.lineStartIndex(pointString, e.getErrorOffset());
            final int end = StringUtil.lineStartIndex(pointString, e.getErrorOffset() + 1);
            if (start < end) {
                final Spannable span = new SpannableString(pointString);
                final int highLightColor = getResources().getColor(android.R.color.holo_red_dark);
                final ForegroundColorSpan highlight = new ForegroundColorSpan(highLightColor);
                span.setSpan(highlight, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                binding.laserText.setText(span);
            } else {
                Log.w(TAG, "No line " + e.getErrorOffset() + " " + pointString);
            }
            binding.laserText.setSelection(end - 1);
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
            editLayer = new LaserProfileLayer(laserProfile, 0xffee1111, 2f);
            clearForm();
            // Re-add edit layer since it will be removed on fragment stop
            Services.lasers.layers.add(editLayer);
            // Return to main fragment
            getParentFragmentManager().popBackStack();
        } else {
            // Form error
            Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called when user clicks rangefinder button
     */
    private void clickLaserConnect(View view) {
        Analytics.logEvent(getContext(), "click_laser_edit_connect", null);
        rangefinderEnabled = true;
        updateRangefinder();
        laserConnect();
    }

    private boolean checkPermissions(@NonNull Activity activity) {
        // Check for location permissions
        if (!Permissions.hasLocationPermissions(activity)) {
            Log.w(TAG, "Location permission required");
            errorMessage = "Location permission required";
            requestPermissions(Permissions.btPermissions(), RequestCodes.RC_BLUE_ALL);
            return false;
        }
        // Check for location services enabled. Can't scan without location
        if (!Permissions.isLocationServiceEnabled(activity)) {
            Log.w(TAG, "Location service disabled");
            errorMessage = "Location service disabled";
            // Request to enable location services
            promptForLocation(activity);
            return false;
        }
        // Check bluetooth permissions
        if (!Permissions.hasBluetoothPermissions(activity)) {
            Log.w(TAG, "Bluetooth permissions required");
            errorMessage = "Bluetooth permission required";
            requestPermissions(Permissions.btPermissions(), RequestCodes.RC_BLUE_ALL);
            return false;
        }
        // TODO: Check if bluetooth is enabled
        return true;
    }

    private void laserConnect() {
        final Activity activity = getActivity();
        if (activity != null) {
            if (checkPermissions(activity)) {
                rangefinder.start(activity);
            }
        }
    }

    /**
     * Prompt user to enable location services
     */
    private void promptForLocation(@NonNull Context context) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Location is required for bluetooth, please enable location services")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> Permissions.openLocationSettings(context))
                .setNegativeButton("No", (dialog, id) -> {
                    // Revert bluetooth state
                    rangefinderEnabled = false;
                    updateRangefinder();
                    dialog.cancel();
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Clear points button
     */
    private void clickClear(View view) {
        Analytics.logEvent(getContext(), "click_laser_edit_clear", null);
        binding.laserText.setText("");
    }

    /**
     * Sort points button
     */
    private void clickSort(View view) {
        Analytics.logEvent(getContext(), "click_laser_edit_sort", null);
        // Sort by horizontal distance
        final boolean metric = isMetric();
        final List<LaserMeasurement> points = LaserMeasurement.parseSafe(binding.laserText.getText().toString(), metric);
        Collections.sort(points, (l1, l2) -> Double.compare(l1.x, l2.x));
        // Update text box
        binding.laserText.setText(LaserMeasurement.render(points, metric));
        // Update chart in parent activity
        updateLayers();
    }

    /**
     * Cancel laser: clear form and return
     */
    private void laserCancel(View view) {
        Analytics.logEvent(getContext(), "click_laser_edit_cancel", null);
        clearForm();
        getParentFragmentManager().popBackStack();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLaserMeasure(@NonNull LaserMeasurement meas) {
        // Insert newline if needed
        final String text = binding.laserText.getText().toString();
        if (!text.isEmpty() && !text.endsWith("\n")) {
            binding.laserText.append("\n");
        }
        final boolean metric = isMetric();
        final double units = metric ? 1 : 3.28084;
        final String unitLabel = metric ? "m" : "ft";
        // Append measurement to laser text
        final String line = String.format(Locale.US, "%.1f %s, %.1f %s\n", meas.x * units, unitLabel, meas.y * units, unitLabel);
        binding.laserText.append(line);
        // Update chart in parent activity
        updateLayers();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothEvent(@NonNull RangefinderEvent event) {
        updateRangefinder();
    }

    /**
     * Update rangefinder status views
     */
    private void updateRangefinder() {
        if (errorMessage != null) {
            binding.laserStatus.setText(errorMessage);
            binding.laserStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.warning, 0, 0, 0);
            binding.laserStatus.setVisibility(View.VISIBLE);
        } else {
            binding.laserStatus.setVisibility(View.GONE);
        }
        if (rangefinderEnabled) {
            binding.laserConnect.setVisibility(View.GONE);
            if (rangefinder.getState() == BT_CONNECTED) {
                binding.laserStatus.setText(R.string.rangefinder_connected);
                binding.laserStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.status_green, 0, 0, 0);
            } else if (rangefinder.getState() == BT_CONNECTING) {
                binding.laserStatus.setText(R.string.rangefinder_connecting);
                binding.laserStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.status_red, 0, 0, 0);
            } else {
                binding.laserStatus.setText(R.string.rangefinder_searching);
                binding.laserStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.status_red, 0, 0, 0);
            }
            binding.laserStatus.setVisibility(View.VISIBLE);
        } else {
            binding.laserConnect.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.RC_BLUE_ENABLE) {
            if (resultCode == Activity.RESULT_OK) {
                Log.i(TAG, "Bluetooth enabled");
                // Notify rangefinder service that bluetooth was enabled
                rangefinder.bluetoothStarted(getActivity());
            } else {
                Log.w(TAG, "Failed to enable bluetooth");
                rangefinderEnabled = false;
                rangefinder.stop();
                updateRangefinder();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == RequestCodes.RC_LOCATION) {
            if (grantResults.length == 1 && permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Location permission granted");
                    errorMessage = null;
                } else {
                    Log.w(TAG, "Location permission denied");
                    errorMessage = "Location permission denied";
                    rangefinderEnabled = false;
                    rangefinder.stop();
                    updateRangefinder();
                }
            }
        }
        if (requestCode == RequestCodes.RC_BLUE_ALL) {
            int success = 0;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    success++;
                }
            }
            if (permissions.length == success) {
                Log.i(TAG, "Bluetooth permission granted");
                errorMessage = null;
                final Context context = getContext();
                if (context != null) {
                    rangefinder.start(context);
                }
            } else {
                Log.w(TAG, "Bluetooth permission not granted");
                rangefinderEnabled = false;
            }
            updateRangefinder();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void loadForm() {
        final Context context = getContext();
        if (context != null) {
            final NewLaserForm form = NewLaserForm.load(context);
            binding.laserName.setText(form.name);
            binding.laserUnits.setSelection(form.metric ? 0 : 1);
            binding.laserLocation.setText(form.latLngAlt);
            binding.laserText.setText(form.points);
        }
    }

    private void saveForm() {
        final Context context = getContext();
        if (context != null) {
            new NewLaserForm(
                    binding.laserName.getText().toString(),
                    isMetric(),
                    binding.laserLocation.getText().toString(),
                    binding.laserText.getText().toString()
            ).save(context);
        }
    }

    private void clearForm() {
        binding.laserName.setText("");
        binding.laserLocation.setText("");
        binding.laserText.setText("");
    }

    @Override
    public void onLocationChanged(@NonNull MLocation loc) {
        if (defaultLocation == null) {
            defaultLocation = loc;
            final Activity activity = getActivity();
            if (activity != null) {
                activity.runOnUiThread(() -> {
                    // Fill in lat,lon
                    binding.laserLocation.setText(LatLngAlt.formatLatLngAlt(loc.latitude, loc.longitude, loc.altitude_gps));
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
