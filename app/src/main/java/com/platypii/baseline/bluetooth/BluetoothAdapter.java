package com.platypii.baseline.bluetooth;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.List;

class BluetoothAdapter extends BaseAdapter {

    private final List<BluetoothDevice> devices;

    private final LayoutInflater inflater;

    BluetoothAdapter(@NonNull Context context, @NonNull List<BluetoothDevice> devices) {
        this.devices = devices;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final BluetoothDevice device = devices.get(position);

        if(convertView == null) {
            convertView = inflater.inflate(R.layout.bluetooth_list_item, parent, false);
        }

        // Update views
        final TextView nameView = convertView.findViewById(R.id.bluetooth_name);
        final TextView addressView = convertView.findViewById(R.id.bluetooth_id);
        final ImageView checkedView = convertView.findViewById(R.id.bluetooth_checked);
        nameView.setText(device.getName());
        addressView.setText(device.getAddress());
        if(device.getAddress().equals(Services.bluetooth.preferenceDeviceId)) {
            checkedView.setVisibility(View.VISIBLE);
        } else {
            checkedView.setVisibility(View.GONE);
        }

        return convertView;
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
    public long getItemId(int position) {
        return position;
    }

}
