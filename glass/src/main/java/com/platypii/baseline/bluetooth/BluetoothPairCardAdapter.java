package com.platypii.baseline.bluetooth;

import com.platypii.baseline.Services;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.glass.widget.CardScrollAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class that handles list of bluetooth devices available to pair.
 */
public class BluetoothPairCardAdapter extends CardScrollAdapter {

    private final Context context;
    private final List<BluetoothDevice> devices;

    public BluetoothPairCardAdapter(Context context) {
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
        final BluetoothDevice device = (BluetoothDevice) item;
        return devices.indexOf(device);
    }
}