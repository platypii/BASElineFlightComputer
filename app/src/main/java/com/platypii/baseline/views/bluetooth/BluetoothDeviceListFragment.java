package com.platypii.baseline.views.bluetooth;

import com.platypii.baseline.Services;
import com.platypii.baseline.bluetooth.BluetoothDeviceComparator;
import com.platypii.baseline.events.BluetoothEvent;
import android.app.ListFragment;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class BluetoothDeviceListFragment extends ListFragment {
    private static final String TAG = "BluetoothActivity";

    private final List<BluetoothDevice> devices = new ArrayList<>();
    private BluetoothAdapter bluetoothAdapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set list adapter
        bluetoothAdapter = new BluetoothAdapter(getActivity(), devices);
        setListAdapter(bluetoothAdapter);
    }

    private void updateDeviceList() {
        devices.clear();
        final Set<BluetoothDevice> updatedDevices = Services.bluetooth.getDevices();
        if (updatedDevices != null) {
            devices.addAll(updatedDevices);
            // Sort devices to put GPS at top of list
            Collections.sort(devices, new BluetoothDeviceComparator());
        }
        bluetoothAdapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(@NonNull ListView l, View v, int position, long id) {
        final BluetoothDevice device = (BluetoothDevice) l.getItemAtPosition(position);
        Log.i(TAG, "Bluetooth device selected: " + device.getName());
        // Save device preference
        Services.bluetooth.preferences.save(getActivity(), true, device.getAddress(), device.getName());
        Services.bluetooth.restart(getActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        // Listen for bluetooth updates
        EventBus.getDefault().register(this);
        updateDeviceList();
    }
    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBluetoothEvent(BluetoothEvent event) {
        updateDeviceList();
    }

}
