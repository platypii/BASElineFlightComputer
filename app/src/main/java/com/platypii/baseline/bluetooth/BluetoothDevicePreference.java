package com.platypii.baseline.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import com.platypii.baseline.Services;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Custom list preference to select from available bluetooth devices
 */
public class BluetoothDevicePreference extends ListPreference {

    private final Map<String,String> deviceNames = new HashMap<>();

    public BluetoothDevicePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BluetoothDevicePreference(Context context) {
        super(context);
    }

    @Override
    protected View onCreateDialogView() {
        final ListView view = new ListView(getContext());
        final ListAdapter adapter = new ArrayAdapter(getContext(), android.R.layout.select_dialog_singlechoice);
        view.setAdapter(adapter);
        updateEntries();
        return view;
    }

    private void updateEntries() {
        final Set<BluetoothDevice> devices = Services.bluetooth.getDevices();
        if(devices != null) {
            final CharSequence[] entries = new CharSequence[devices.size()];
            final CharSequence[] entryValues = new CharSequence[devices.size()];
            int i = 0;
            deviceNames.clear();
            // Add devices in two passes
            // First pass, likely GPS devices
            for(BluetoothDevice device : devices) {
                if(isGPS(device)) {
                    deviceNames.put(device.getAddress(), device.getName());
                    entries[i] = device.getName();
                    entryValues[i] = device.getAddress();
                    i++;
                }
            }
            // Second pass, all the rest
            for(BluetoothDevice device : devices) {
                if(!isGPS(device)) {
                    deviceNames.put(device.getAddress(), device.getName());
                    entries[i] = device.getName();
                    entryValues[i] = device.getAddress();
                    i++;
                }
            }

            // Populate list
            setEntries(entries);
            setEntryValues(entryValues);
            notifyChanged();
        } else {
            setEntries(new CharSequence[0]);
            setEntryValues(new CharSequence[0]);
        }
    }

    // How to detect bluetooth gps:
    // - device name contains "GPS"
    // - device name starts with "XGPS160-"
    // - device.getBluetoothClass().getDeviceClass() == 1804
    private static boolean isGPS(BluetoothDevice device) {
        return device.getName().contains("GPS");
    }

    public String getName(String id) {
        return deviceNames.get(id);
    }

}
