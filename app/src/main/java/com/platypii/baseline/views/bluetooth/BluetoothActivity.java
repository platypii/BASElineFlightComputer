package com.platypii.baseline.views.bluetooth;

import androidx.annotation.NonNull;
import com.platypii.baseline.Intents;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.bluetooth.BluetoothState;
import com.platypii.baseline.databinding.ActivityBluetoothBinding;
import com.platypii.baseline.events.BluetoothEvent;
import com.platypii.baseline.Permissions;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.PubSub.Subscriber;
import com.platypii.baseline.views.BaseActivity;

import android.os.Bundle;
import android.view.View;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class BluetoothActivity extends BaseActivity implements Subscriber<MLocation> {
    private static final String TAG = "BluetoothActivity";

    private ActivityBluetoothBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBluetoothBinding.inflate(getLayoutInflater());
        binding.btPhone.setOnClickListener(this::clickPhone);
        binding.btPhoneStatus.setOnClickListener(this::clickPhone);
        binding.bluetoothPair.setOnClickListener(this::clickPair);
        setContentView(binding.getRoot());
    }

    private void updateViews() {
        // Check phone permissions
        binding.btPhoneStatus.setText("Phone OK");
        binding.btPhoneStatus.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.status_green, 0, 0);
        if (!Services.bluetooth.preferences.preferenceEnabled) {
            // Check android location permission
            if (!Permissions.hasLocationPermissions(this)) {
                binding.btPhoneStatus.setText("Permission required");
                binding.btPhoneStatus.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.status_red, 0, 0);
            } else if (!Permissions.isLocationServiceEnabled(this)) {
                binding.btPhoneStatus.setText("Location disabled");
                binding.btPhoneStatus.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.status_red, 0, 0);
            }
        } else {
            // Check bluetooth permissions
            if (!Permissions.hasBluetoothPermissions(this)) {
                binding.btPhoneStatus.setText("Permission required");
                binding.btPhoneStatus.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.status_red, 0, 0);
            }
        }
        // Bluetooth status
        if (Services.bluetooth.preferences.preferenceEnabled) {
            binding.btGps.setVisibility(View.VISIBLE);
            binding.btGpsStatus.setVisibility(View.VISIBLE);
        } else {
            binding.btGps.setVisibility(View.GONE);
            binding.btGpsStatus.setVisibility(View.GONE);
        }
        if (Services.bluetooth.preferences.preferenceDeviceName != null) {
            binding.btGpsStatus.setText(Services.bluetooth.preferences.preferenceDeviceName);
        } else {
            binding.btGpsStatus.setText("");
        }
        binding.btGpsStatus.setText(Services.bluetooth.getStatusMessage(this));
        if (Services.bluetooth.getState() == BluetoothState.BT_CONNECTED) {
            binding.btGpsStatus.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.status_green, 0, 0);
        } else {
            binding.btGpsStatus.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.status_red, 0, 0);
        }
        // Satellite signal status
        final long lastFixDuration = Services.location.lastFixDuration();
        if (lastFixDuration < 0) {
            binding.btSatStatus.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.status_red, 0, 0);
            binding.btSatStatus.setText("No fix");
        } else if (lastFixDuration > 5000) {
            binding.btSatStatus.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.status_red, 0, 0);
            binding.btSatStatus.setText("No fix");
        } else if (lastFixDuration > 1100) {
            binding.btSatStatus.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.status_yellow, 0, 0);
            binding.btSatStatus.setText("Last fix " + lastFixDuration / 1000L + "s");
        } else if (Services.location.refreshRate.refreshRate < 2) {
            // 1 hz is not enough
            binding.btSatStatus.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.status_yellow, 0, 0);
            binding.btSatStatus.setText(Services.location.lastLoc.satellitesUsed + " satellites (1 Hz)");
        } else {
            binding.btSatStatus.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.status_green, 0, 0);
            binding.btSatStatus.setText(Services.location.lastLoc.satellitesUsed + " satellites");
        }
    }

    private void clickPhone(View view) {
        if (!Services.bluetooth.preferences.preferenceEnabled) {
            // Check android location permission
            if (!Permissions.hasLocationPermissions(this)) {
                // Request location permissions
                Permissions.requestLocationPermissions(this);
            } else if (!Permissions.isLocationServiceEnabled(this)) {
                // Request to enable location
                Permissions.openLocationSettings(this);
            }
        }
    }

    private void clickPair(View v) {
        Intents.openBluetoothSettings(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Check bluetooth permissions to scan and connect
        if (!Permissions.hasBluetoothPermissions(this)) {
            Permissions.requestBluetoothPermissions(this);
        }

        // Listen for bluetooth updates
        EventBus.getDefault().register(this);
        Services.location.locationUpdates.subscribeMain(this);
        updateViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
        Services.location.locationUpdates.unsubscribeMain(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothEvent(BluetoothEvent event) {
        updateViews();
    }

    @Override
    public void apply(@NonNull MLocation loc) {
        updateViews();
    }

}
