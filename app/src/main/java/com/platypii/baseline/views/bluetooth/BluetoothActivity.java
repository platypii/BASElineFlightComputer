package com.platypii.baseline.views.bluetooth;

import com.platypii.baseline.Intents;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.bluetooth.BluetoothState;
import com.platypii.baseline.databinding.ActivityBluetoothBinding;
import com.platypii.baseline.events.BluetoothEvent;
import com.platypii.baseline.views.BaseActivity;

import android.os.Bundle;
import android.view.View;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class BluetoothActivity extends BaseActivity {
    private static final String TAG = "BluetoothActivity";

    private ActivityBluetoothBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBluetoothBinding.inflate(getLayoutInflater());
        binding.bluetoothPair.setOnClickListener(this::clickPair);
        setContentView(binding.getRoot());
    }

    private void updateViews() {
        if (Services.bluetooth.preferences.preferenceDeviceName != null && Services.bluetooth.preferences.preferenceDeviceName.startsWith("XGPS160")) {
            binding.gpsPhoto.setImageResource(R.drawable.skypro);
        } else {
            binding.gpsPhoto.setImageResource(0);
        }
        binding.bluetoothStatus.setText(Services.bluetooth.getStatusMessage(this));
        if (Services.bluetooth.getState() == BluetoothState.BT_CONNECTED) {
            binding.bluetoothStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.status_green, 0, 0, 0);
        } else {
            binding.bluetoothStatus.setCompoundDrawablesWithIntrinsicBounds(R.drawable.status_red, 0, 0, 0);
        }
    }

    public void clickPair(View v) {
        Intents.openBluetoothSettings(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Listen for bluetooth updates
        EventBus.getDefault().register(this);
        updateViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothEvent(BluetoothEvent event) {
        updateViews();
    }

}
