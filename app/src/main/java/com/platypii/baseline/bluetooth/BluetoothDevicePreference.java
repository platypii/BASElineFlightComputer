package com.platypii.baseline.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import java.util.Set;

public class BluetoothDevicePreference extends ListPreference {

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
        final Set<BluetoothDevice> devices = BluetoothService.getDevices();
        if(devices != null) {
            final CharSequence[] entries = new CharSequence[devices.size()];
            final CharSequence[] entryValues = new CharSequence[devices.size()];
            int i = 0;
            for(BluetoothDevice device : devices) {
                entries[i] = device.getName();
                entryValues[i] = device.getAddress();
                i++;
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

}
