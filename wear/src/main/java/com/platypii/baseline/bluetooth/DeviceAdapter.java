package com.platypii.baseline.bluetooth;

import com.platypii.baseline.R;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.List;

class DeviceAdapter extends ArrayAdapter<BluetoothDevice> {

    private final List<BluetoothDevice> devices;
    private final LayoutInflater inflater;

    DeviceAdapter(@NonNull Context context, int resource, @NonNull List<BluetoothDevice> devices) {
        super(context, resource, devices);
        this.devices = devices;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final BluetoothDevice device = devices.get(position);

        if(convertView == null) {
            convertView = inflater.inflate(R.layout.bluetooth_list_item, parent, false);
        }

        // Update views
        final TextView itemNameView = (TextView) convertView.findViewById(R.id.list_item_name);
        final TextView itemSizeView = (TextView) convertView.findViewById(R.id.list_item_subtitle);
        itemNameView.setText(device.getName());
        itemSizeView.setText(device.getAddress());

        return convertView;
    }

}
