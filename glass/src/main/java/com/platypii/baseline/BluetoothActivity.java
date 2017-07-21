package com.platypii.baseline;

import com.platypii.baseline.bluetooth.BluetoothCardAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import com.google.android.glass.widget.CardScrollView;

public class BluetoothActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private CardScrollView cardScroller;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        cardScroller = new CardScrollView(this);
        cardScroller.setAdapter(new BluetoothCardAdapter(this));
        setContentView(cardScroller);
        cardScroller.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Save bluetooth device
        final BluetoothDevice device = (BluetoothDevice) cardScroller.getItemAtPosition(position);
        Services.bluetooth.preferenceEnabled = true;
        Services.bluetooth.preferenceDeviceId = device.getAddress();
        Services.bluetooth.preferenceDeviceName = device.getName();
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean("bluetooth_enabled", Services.bluetooth.preferenceEnabled);
        edit.putString("bluetooth_device_id", Services.bluetooth.preferenceDeviceId);
        edit.putString("bluetooth_device_name", Services.bluetooth.preferenceDeviceName);
        edit.apply();
        Services.bluetooth.restart(this);
        finish();
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

}
