package com.platypii.baseline.views.bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.platypii.baseline.Services;
import com.platypii.baseline.util.Analytics;
import com.platypii.baseline.util.Exceptions;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

public class BleDeviceListFragment extends ListFragment {
    private static final String TAG = "BleDeviceList";

    private final List<BluetoothDevice> devices = new ArrayList<>();
    @Nullable
    private BluetoothAdapter bluetoothAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Set list adapter
        final Activity activity = getActivity();
        if (activity != null) {
            bluetoothAdapter = new BluetoothAdapter(activity, devices, false);
            setListAdapter(bluetoothAdapter);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    private void updateDeviceList() {
        if (bluetoothAdapter != null) {
            bluetoothAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        final BluetoothDevice device = (BluetoothDevice) l.getItemAtPosition(position);
        final Activity activity = getActivity();
        if (activity != null) {
            if (device != null) {
                final String deviceName = device.getName();
                Log.i(TAG, "Bluetooth device selected " + deviceName);

                // Log event
                final Bundle bundle = new Bundle();
                bundle.putString("device_id", device.getAddress());
                bundle.putString("device_name", deviceName);
                Analytics.logEvent(activity, "ble_selected", bundle);

                // Save device preference
                Services.bleService.doConnect(activity, device);
            }
            // Switch location source
            Services.location.restart(activity);

            // Should we wait for the connection to establish before going back?
            activity.finish();
        } else {
            Exceptions.report(new NullPointerException("Null activity on bluetooth device click"));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDeviceList();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void onScanResult(@NonNull BluetoothDevice device) {
        if(!devices.contains(device)) {
            devices.add(device);
            updateDeviceList();
        }
    }

}
