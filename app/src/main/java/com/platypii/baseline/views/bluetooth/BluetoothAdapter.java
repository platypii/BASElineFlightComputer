package com.platypii.baseline.views.bluetooth;

import android.os.Build;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;

import android.bluetooth.BluetoothDevice;
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

import static com.platypii.baseline.bluetooth.BluetoothUtil.getDeviceName;

class BluetoothAdapter extends BaseAdapter {

    @NonNull
    private final List<BluetoothDevice> devices;

    private final LayoutInflater inflater;

    private final String internalGps;

    BluetoothAdapter(@NonNull Context context, @NonNull List<BluetoothDevice> devices) {
        this.devices = devices;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        internalGps = context.getString(R.string.internal_gps);
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

        if (position == 0) {
            // Internal phone GPS
            nameView.setText(internalGps);
            addressView.setText(Build.MANUFACTURER + " " + Build.MODEL);
            if (!Services.bluetooth.preferences.preferenceEnabled) {
                checkedView.setVisibility(View.VISIBLE);
            } else {
                checkedView.setVisibility(View.GONE);
            }
        } else {
            // Bluetooth GPS device
            final BluetoothDevice device = devices.get(position - 1);
            final String deviceName = getDeviceName(device);
            nameView.setText(deviceName);
            addressView.setText(device.getAddress());
            if (deviceName.contains("GPS") || deviceName.startsWith("Mohawk")) {
                nameView.setTextColor(0xffeeeeee);
            } else {
                nameView.setTextColor(0xffbbbbbb);
            }
            if (device.getAddress().equals(Services.bluetooth.preferences.preferenceDeviceId)) {
                checkedView.setVisibility(View.VISIBLE);
            } else {
                checkedView.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    @Override
    public int getCount() {
        return devices.size() + 1;
    }

    @Override
    public Object getItem(int position) {
        return position == 0 ? null : devices.get(position - 1);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

}
