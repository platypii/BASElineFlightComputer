package com.platypii.baseline.bluetooth;

import com.platypii.baseline.Services;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import com.google.android.glass.widget.CardScrollAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class that handles list of cards.
 */
public class BluetoothCardAdapter extends CardScrollAdapter {

    private final Context context;
    private final List<BluetoothDevice> devices;

    public BluetoothCardAdapter(Context context) {
        this.context = context;
        this.devices = new ArrayList<BluetoothDevice>(Services.bluetooth.getDevices());
    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return devices.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = new TextView(context);
        }
        final BluetoothDevice device = (BluetoothDevice) getItem(position);
        ((TextView) convertView).setText(device.getName());
        return convertView;
    }

    @Override
    public int getPosition(Object item) {
        for (int i = 0; i < devices.size(); i++) {
            if (getItem(i).equals(item)) {
                return i;
            }
        }
        return AdapterView.INVALID_POSITION;
    }
}