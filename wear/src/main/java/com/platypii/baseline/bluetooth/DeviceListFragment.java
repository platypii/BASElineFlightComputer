package com.platypii.baseline.bluetooth;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import android.app.ListFragment;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

public class DeviceListFragment extends ListFragment implements AdapterView.OnItemClickListener {

    private List<BluetoothDevice> devices;
    private ArrayAdapter<BluetoothDevice> listAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bluetooth_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Initialize the ListAdapter
        devices = new ArrayList<>();
        listAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, devices);
        setListAdapter(listAdapter);
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Update the views
        updateList();
    }

    private void updateList() {
        // Update list from track cache
        devices.clear();
        devices.addAll(Services.bluetooth.getDevices());
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final BluetoothDevice device = devices.get(position);
        // Save bluetooth device to preferences
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        final SharedPreferences.Editor editor = prefs.edit();
        editor.putString("bluetooth_device_id", device.getAddress());
        editor.putString("bluetooth_device_name", device.getName());
        editor.apply();
        // Start / restart bluetooth service
        Services.bluetooth.restart(getActivity());
    }
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
    }

}
