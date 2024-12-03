package com.platypii.baseline.views.bluetooth;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.bluetooth.BluetoothItem;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

class BluetoothAdapter extends BaseAdapter {

    @NonNull
    private final List<BluetoothItem> devices;

    private final LayoutInflater inflater;

    BluetoothAdapter(@NonNull Context context, @NonNull List<BluetoothItem> devices) {
        this.devices = devices;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.bluetooth_list_item, parent, false);
        }

        // Update views
        final TextView nameView = convertView.findViewById(R.id.bluetooth_name);
        final TextView addressView = convertView.findViewById(R.id.bluetooth_id);
        final ImageView checkedView = convertView.findViewById(R.id.bluetooth_checked);

        // Bluetooth GPS device
        final BluetoothItem device = devices.get(position);
        nameView.setText(device.name);
        addressView.setText(device.address);
        if (device.internal) {
            // Internal phone GPS
            if (!Services.bluetooth.preferences.preferenceEnabled) {
                checkedView.setVisibility(View.VISIBLE);
            } else {
                checkedView.setVisibility(View.GONE);
            }
        } else {
            if (device.name.contains("GPS") || device.name.startsWith("FlySight") || device.name.startsWith("Mohawk")) {
                nameView.setTextColor(0xffeeeeee);
            } else {
                nameView.setTextColor(0xffbbbbbb);
            }
            if (device.address.equals(Services.bluetooth.preferences.preferenceDeviceId)) {
                checkedView.setVisibility(View.VISIBLE);
            } else {
                checkedView.setVisibility(View.GONE);
            }
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
