package com.platypii.baseline.views.bluetooth;

import com.platypii.baseline.Services;
import com.platypii.baseline.bluetooth.BluetoothItem;
import com.platypii.baseline.events.BluetoothEvent;
import com.platypii.baseline.util.Analytics;
import com.platypii.baseline.util.Exceptions;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import androidx.annotation.NonNull;
import com.welie.blessed.BluetoothPeripheral;
import java.util.HashSet;
import java.util.Set;
import org.greenrobot.eventbus.EventBus;

public class FlysightDeviceListFragment extends BluetoothDeviceListFragment {
    private static final String TAG = "FlysightDeviceList";

    @Override
    protected Set<BluetoothItem> getDeviceList() {
        final Set<BluetoothItem> devices = new HashSet<>();
        for (BluetoothPeripheral peripheral : Services.bluetooth.flysightProtocol.scanResults) {
            devices.add(new BluetoothItem(peripheral));
        }
        return devices;
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id) {
        final BluetoothItem device = (BluetoothItem) l.getItemAtPosition(position);
        final Activity activity = getActivity();
        if (activity != null) {
            // If current device is clicked, deactivate
            if (!device.address.equals(Services.bluetooth.preferences.preferenceDeviceId)) {
                Log.i(TAG, "FlySight selected " + device.name);

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
                Log.i(TAG, "FlySight deselected");
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
            Exceptions.report(new NullPointerException("Null activity on flysight device click"));
        }
    }
}
