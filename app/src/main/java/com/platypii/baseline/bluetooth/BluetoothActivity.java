package com.platypii.baseline.bluetooth;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.events.BluetoothEvent;
import android.app.ListActivity;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.google.firebase.analytics.FirebaseAnalytics;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class BluetoothActivity extends ListActivity {
    private static final String TAG = "BluetoothActivity";

    private FirebaseAnalytics firebaseAnalytics;

    private Button bluetoothButton;
    private TextView bluetoothStatus;

    private final List<BluetoothDevice> devices = new ArrayList<>();
    private BluetoothAdapter bluetoothAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        firebaseAnalytics = FirebaseAnalytics.getInstance(this);

        bluetoothButton = findViewById(R.id.button_bluetooth);
        bluetoothStatus = findViewById(R.id.bluetooth_status);

        // Update device list
        bluetoothAdapter = new BluetoothAdapter(this, devices);
        setListAdapter(bluetoothAdapter);
    }

    private void updateViews() {
        if (Services.bluetooth.preferenceEnabled) {
            bluetoothButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.bluetooth_on, 0, 0);
            bluetoothButton.setText("Enabled");
        } else {
            bluetoothButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.bluetooth, 0, 0);
            bluetoothButton.setText("Disabled");
        }
        bluetoothStatus.setText(Services.bluetooth.getStatusMessage(this));
        updateDeviceList();
    }

    private void updateDeviceList() {
        devices.clear();
        devices.addAll(Services.bluetooth.getDevices());
        // Sort devices to put GPS at top of list
        Collections.sort(devices, new Comparator<BluetoothDevice>() {
            @Override
            public int compare(BluetoothDevice device1, BluetoothDevice device2) {
                return score(device2) - score(device1);
            }
            private int score(BluetoothDevice device) {
                if (device.getAddress().equals(Services.bluetooth.preferenceDeviceId)) {
                    return 2;
                } else if (device.getName().contains("GPS")) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });
        bluetoothAdapter.notifyDataSetChanged();
    }

    public void clickEnable(View v) {
        // Start or stop bluetooth
        Services.bluetooth.preferenceEnabled = !Services.bluetooth.preferenceEnabled;
        if(Services.bluetooth.preferenceEnabled) {
            Log.i(TAG, "User clicked bluetooth enable");
            firebaseAnalytics.logEvent("bluetooth_enabled", null);
            Services.bluetooth.start(this);
        } else {
            Log.i(TAG, "User clicked bluetooth disable");
            firebaseAnalytics.logEvent("bluetooth_disabled", null);
            Services.bluetooth.stop();
        }
        // Save preference
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("bluetooth_enabled", Services.bluetooth.preferenceEnabled);
        edit.apply();
        updateViews();
    }

    public void clickPair(View v) {
        // Open bluetooth settings
        final Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.bluetooth.BluetoothSettings");
        intent.setComponent(cn);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        final BluetoothDevice device = (BluetoothDevice) l.getItemAtPosition(position);
        Services.bluetooth.preferenceDeviceId = device.getAddress();
        Services.bluetooth.preferenceDeviceName = device.getName();
        Log.i(TAG, "Bluetooth device selected: " + Services.bluetooth.preferenceDeviceId);
        Services.bluetooth.restart(this);
        // Save device preference
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putString("bluetooth_device_id", Services.bluetooth.preferenceDeviceId);
        edit.putString("bluetooth_device_name", Services.bluetooth.preferenceDeviceName);
        edit.apply();
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
