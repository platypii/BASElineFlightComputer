package com.platypii.baseline.views;

import java.lang.reflect.Method;
import com.platypii.baseline.bluetooth.BluetoothCardAdapter;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import com.google.android.glass.widget.CardScrollView;
import java.util.ArrayList;
import java.util.List;

public class BluetoothPairActivity extends BaseActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "BluetoothPairActivity";

    private CardScrollView cardScroller;
    private BluetoothCardAdapter cardAdapter;
    private BluetoothAdapter bluetoothAdapter;
    private final List<BluetoothDevice> devices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(bluetoothBroadcastReceiver, filter);

        bluetoothAdapter.startDiscovery();
        Log.d("onCreate", "Started BT discovery...");

        cardScroller = new CardScrollView(this);
        cardAdapter = new BluetoothCardAdapter(this, devices);
        cardScroller.setAdapter(cardAdapter);
        setContentView(cardScroller);
        cardScroller.setOnItemClickListener(this);
    }

    private final BroadcastReceiver bluetoothBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                final String deviceName = device.getName();
                if (deviceName != null && deviceName.contains("GPS")) {
                    devices.add(0, device);
                } else {
                    devices.add(device);
                }
                runOnUiThread(() -> {
                    cardAdapter = new BluetoothCardAdapter(BluetoothPairActivity.this, devices);
                    cardScroller.setAdapter(cardAdapter);
                });
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final BluetoothDevice device = devices.get(position);
        Log.i(TAG, "Pairing BT device " + device.getName());
        pairDevice(device);
        // TODO: Go to bluetooth activity
        finish();
    }

    private void pairDevice(BluetoothDevice device) {
        try {
            final Method m = device.getClass().getMethod("createBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e(TAG, "BT pairing exception", e);
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cardScroller.activate();
    }

    @Override
    protected void onPause() {
        cardScroller.deactivate();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Cancel discovery
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            Log.i(TAG, "Canceled BT discovery");
        }
        unregisterReceiver(bluetoothBroadcastReceiver);
    }

}
