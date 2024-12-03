package com.platypii.baseline.views;

import com.platypii.baseline.Permissions;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.bluetooth.BluetoothState;
import com.platypii.baseline.databinding.ActivityFlysightBinding;
import com.platypii.baseline.events.BluetoothEvent;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.util.Numbers;
import com.platypii.baseline.util.PubSub.Subscriber;

import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.Locale;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class FlysightActivity extends BaseActivity implements Subscriber<MLocation> {

    private ActivityFlysightBinding binding;

    // Periodic UI updates
    private final Handler handler = new Handler();
    private final int updateInterval = 100; // in milliseconds
    @Nullable
    private Runnable updateRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFlysightBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Start GPS updates
        Services.location.locationUpdates.subscribeMain(this);
        updateGPS();

        // Start event updates
        EventBus.getDefault().register(this);

        // Start BLE scanning
        if (Services.bluetooth.ble.bluetoothState == BluetoothState.BT_STOPPED) {
            Services.bluetooth.ble.start(this);
        }

        // Periodic UI updates
        updateRunnable = new Runnable() {
            public void run() {
                update();
                handler.postDelayed(this, updateInterval);
            }
        };
        handler.post(updateRunnable);
        update();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(updateRunnable);
        updateRunnable = null;
        EventBus.getDefault().unregister(this);
        Services.location.locationUpdates.unsubscribeMain(this);
    }

    private void updateGPS() {
        final MLocation loc = Services.location.lastLoc;
        // TODO: Only from flysight
        if (loc != null && Services.bluetooth.preferences.preferenceEnabled) {
            if (Numbers.isReal(loc.latitude) && Numbers.isReal(loc.latitude)) {
                String ll = String.format(Locale.getDefault(), "%.6f, %.6f", loc.latitude, loc.longitude);
                if (Numbers.isReal(loc.altitude_gps)) {
                    ll += ", " + Convert.distance(loc.altitude_gps, 2, true);
                }
                if (Numbers.isReal(loc.groundSpeed())) {
                    ll += ", " + Convert.speed(loc.groundSpeed(), 2, true);
                }
                binding.flysightGpsLabel.setText(ll);
            } else {
                binding.flysightGpsLabel.setText("");
            }
        } else {
            binding.flysightGpsLabel.setText("");
        }
    }

    /**
     * Updates the UI that refresh continuously, such as sample rates
     */
    private void update() {
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
            // FlySight selected for GPS
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
                binding.btSatStatus.setText("Last fix " + lastFixDuration / 1000L + "s"); // TODO: Periodic updater for last fix
            } else {
                final int hz = (int)(Services.location.refreshRate() + 0.5f);
                // 1 hz is not enough
                if (hz >= 2) {
                    binding.btSatStatus.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.status_green, 0, 0);
                } else {
                    binding.btSatStatus.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.status_yellow, 0, 0);
                }
                binding.btSatStatus.setText(String.format(Locale.getDefault(), "%d Hz", hz));
            }
        } else {
            // FlySight not selected for GPS
            binding.btGpsStatus.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.status_red, 0, 0);
            binding.btGpsStatus.setText("FS not selected");
            binding.btSatStatus.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.status_red, 0, 0);
            binding.btSatStatus.setText("FS GPS off");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Services.sensors.gravity.setMaxSize(0);
        Services.sensors.rotation.setMaxSize(0);
    }

    // Listeners
    @Override
    public void apply(@NonNull MLocation loc) {
        updateGPS();
    }

    /**
     * Listen for altitude updates
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothEvent(BluetoothEvent event) {
        updateGPS();
    }

}
