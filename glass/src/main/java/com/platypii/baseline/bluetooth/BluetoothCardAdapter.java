package com.platypii.baseline.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.android.glass.widget.CardScrollAdapter;
import java.util.List;

/**
 * Adapter class that handles list of cards.
 */
public class BluetoothCardAdapter extends CardScrollAdapter {

    private final Context context;
    private final List<BluetoothDevice> devices;

    public BluetoothCardAdapter(Context context, List<BluetoothDevice> devices) {
        this.context = context;
        this.devices = devices;
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
        if (convertView == null) {
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