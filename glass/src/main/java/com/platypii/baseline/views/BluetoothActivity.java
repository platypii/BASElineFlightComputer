package com.platypii.baseline.views;

import com.platypii.baseline.Services;
import com.platypii.baseline.bluetooth.BluetoothCardAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import com.google.android.glass.widget.CardScrollView;
import java.util.ArrayList;
import java.util.List;

public class BluetoothActivity extends BaseActivity implements AdapterView.OnItemClickListener {

    private CardScrollView cardScroller;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        final List<BluetoothDevice> devices = new ArrayList<>(Services.bluetooth.getDevices());
        cardScroller = new CardScrollView(this);
        cardScroller.setAdapter(new BluetoothCardAdapter(this, devices));
        setContentView(cardScroller);
        cardScroller.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Save bluetooth device
        final BluetoothDevice device = (BluetoothDevice) cardScroller.getItemAtPosition(position);
        Services.bluetooth.preferences.save(this, true, device.getAddress(), device.getName());
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
