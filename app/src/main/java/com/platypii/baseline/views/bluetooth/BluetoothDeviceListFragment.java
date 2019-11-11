package com.platypii.baseline.views.bluetooth;

import com.platypii.baseline.Services;
import com.platypii.baseline.events.BluetoothEvent;
import com.platypii.baseline.util.Analytics;
import com.platypii.baseline.util.Exceptions;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import java.util.ArrayList;
import java.util.List;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class BluetoothDeviceListFragment extends ListFragment {
    private static final String TAG = "BluetoothDeviceList";

    private final List<BluetoothDevice> devices = new ArrayList<>();
    @Nullable
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
        final List<BluetoothDevice> updatedDevices = Services.bluetooth.getDevices();
        devices.addAll(updatedDevices);
        if (bluetoothAdapter != null) {
            bluetoothAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        final BluetoothDevice device = (BluetoothDevice) l.getItemAtPosition(position);
        Log.i(TAG, "Bluetooth device selected: " + device.getName());
        final Activity activity = getActivity();
        if (activity != null) {
            // Log event
            final Bundle bundle = new Bundle();
            bundle.putString("device_id", device.getAddress());
            bundle.putString("device_name", device.getName());
            Analytics.logEvent(activity, "bluetooth_selected", bundle);
            // Save device preference
            Services.bluetooth.preferences.save(activity, true, device.getAddress(), device.getName());
            Services.bluetooth.restart(activity);
        } else {
            Exceptions.report(new NullPointerException("Null activity on bluetooth device click"));
        }
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
