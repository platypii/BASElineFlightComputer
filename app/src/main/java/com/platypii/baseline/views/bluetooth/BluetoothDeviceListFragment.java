package com.platypii.baseline.views.bluetooth;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.bluetooth.BluetoothDeviceComparator;
import com.platypii.baseline.bluetooth.BluetoothItem;
import com.platypii.baseline.events.BluetoothEvent;
import com.platypii.baseline.util.Analytics;
import com.platypii.baseline.util.Exceptions;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class BluetoothDeviceListFragment extends ListFragment {
    private static final String TAG = "BluetoothDeviceList";

    private final List<BluetoothItem> devices = new ArrayList<>();
    private final Set<BluetoothItem> deviceSet = new HashSet<>();
    @Nullable
    private BluetoothAdapter bluetoothAdapter;
    private String internalGps = "Phone GPS"; // cached "Phone GPS" string from context

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Set list adapter
        final Activity activity = getActivity();
        if (activity != null) {
            internalGps = activity.getString(R.string.internal_gps);
            bluetoothAdapter = new BluetoothAdapter(activity, devices);
            setListAdapter(bluetoothAdapter);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    protected Set<BluetoothItem> getDeviceList() {
        final Set<BluetoothItem> devices = new HashSet<>();
        for (BluetoothDevice device : Services.bluetooth.getBondedDevices()) {
            devices.add(new BluetoothItem(device));
        }
        // Add internal phone gps
        final String phoneName = Build.MANUFACTURER + " " + Build.MODEL;
        deviceSet.add(new BluetoothItem(internalGps, phoneName, false, true));
        return devices;
    }

    private void updateDeviceList() {
        devices.clear();
        deviceSet.clear();
        try {
            deviceSet.addAll(getDeviceList());
        } catch (SecurityException e) {
            Log.e(TAG, "Error getting device list", e);
        }
        devices.addAll(deviceSet);
        devices.sort(new BluetoothDeviceComparator());
        if (bluetoothAdapter != null) {
            bluetoothAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        final BluetoothItem device = (BluetoothItem) l.getItemAtPosition(position);
        final Activity activity = getActivity();
        if (activity != null) {
            if (!device.internal) {
                Log.i(TAG, "Bluetooth device selected " + device.name);

                if (!device.address.equals(Services.bluetooth.preferences.preferenceDeviceId)) {
                    // Changed bluetooth device, reset state
                    Services.location.locationProviderBluetooth.reset();
                }

                // Save bluetooth device and enable bluetooth
                Services.bluetooth.preferences.save(activity, true, device.address, device.name, device.ble);
                // Update ui
                EventBus.getDefault().post(new BluetoothEvent());
                // Start / restart bluetooth service
                Services.bluetooth.restart(activity);

                // Log event
                final Bundle bundle = new Bundle();
                bundle.putString("device_id", device.address);
                bundle.putString("device_name", device.name);
                Analytics.logEvent(activity, "bluetooth_selected", bundle);
            } else {
                Log.i(TAG, "Internal GPS selected");
                // Clear bluetooth device and disable bluetooth
                Services.bluetooth.preferences.save(activity, false, null, null, false);
                // Update ui
                EventBus.getDefault().post(new BluetoothEvent());
                // Stop bluetooth service if needed
                Services.bluetooth.stop();
            }
            // Update ui
            EventBus.getDefault().post(new BluetoothEvent());
            // Switch location source
            Services.location.restart(activity);
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
    public void onBluetoothEvent(@NonNull BluetoothEvent event) {
        updateDeviceList();
    }

}
