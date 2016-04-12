package com.platypii.baseline;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;

import com.platypii.baseline.bluetooth.BluetoothService;
import com.platypii.baseline.data.Convert;
import com.platypii.baseline.util.Util;

/**
 * Settings activity for things like metric / imperial
 */
public class SettingsActivity extends PreferenceActivity {
    private static final String TAG = "Settings";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new GeneralPreferenceFragment())
                .commit();
    }

    /**
     * This fragment shows the preferences
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class GeneralPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        private CheckBoxPreference metricPreference;
        private SwitchPreference bluetoothPreference;
        private ListPreference bluetoothDevicePreference;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            metricPreference = (CheckBoxPreference) findPreference("metric_enabled");
            metricPreference.setOnPreferenceChangeListener(this);

            bluetoothPreference = (SwitchPreference) findPreference("bluetooth_enabled");
            bluetoothPreference.setOnPreferenceChangeListener(this);
            bluetoothDevicePreference = (ListPreference) findPreference("bluetooth_device");
            bluetoothDevicePreference.setOnPreferenceChangeListener(this);

            updateViews();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            switch(preference.getKey()) {
                case "metric_enabled":
                    Convert.metric = (Boolean) value;
                    Log.i(TAG, "Setting metric mode: " + Convert.metric);

                    // Convert audible min and max
                    final SharedPreferences prefs = preference.getSharedPreferences();
                    double audible_min = Util.parseDouble(prefs.getString("audible_min", null));
                    double audible_max = Util.parseDouble(prefs.getString("audible_max", null));
                    final SharedPreferences.Editor edit = prefs.edit();
                    if(Convert.metric) {
                        // Convert mph -> km/h
                        audible_min = Convert.mph2kph(audible_min);
                        audible_max = Convert.mph2kph(audible_max);
                    } else {
                        audible_min = Convert.kph2mph(audible_min);
                        audible_max = Convert.kph2mph(audible_max);
                    }
                    edit.putString("audible_min", Double.toString(audible_min));
                    edit.putString("audible_max", Double.toString(audible_max));
                    edit.apply();

                    break;
                case "bluetooth_enabled":
                    BluetoothService.preferenceEnabled = (Boolean) value;
                    if(BluetoothService.preferenceEnabled) {
                        BluetoothService.startAsync(this.getActivity());
                    } else {
                        BluetoothService.stop();
                    }
                    break;
                case "bluetooth_device":
                    BluetoothService.preferenceDevice = (String) value;
                    Log.i(TAG, "Bluetooth device selected: " + BluetoothService.preferenceDevice);
                    BluetoothService.stop();
                    BluetoothService.startAsync(this.getActivity());
                    break;
            }
            updateViews();
            return true;
        }

        private void updateViews() {
            if(Convert.metric) {
                metricPreference.setSummary("Current units: metric");
            } else {
                metricPreference.setSummary("Current units: imperial");
            }
            if(BluetoothService.preferenceEnabled) {
                bluetoothPreference.setSummary(R.string.pref_bluetooth_enabled);
            } else {
                bluetoothPreference.setSummary(R.string.pref_bluetooth_disabled);
            }
            bluetoothDevicePreference.setEnabled(BluetoothService.preferenceEnabled);
            if(BluetoothService.preferenceDevice != null) {
                final BluetoothDevice device = BluetoothService.getDevice();
                if(device != null) {
                    bluetoothDevicePreference.setSummary(device.getName());
                }
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Start flight services
        Services.start(this);
    }
    @Override
    public void onStop() {
        super.onStop();
        // Stop flight services
        Services.stop();
    }

}
