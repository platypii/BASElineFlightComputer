package com.platypii.baseline;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.platypii.baseline.bluetooth.BluetoothDevicePreference;
import com.platypii.baseline.bluetooth.BluetoothService;
import com.platypii.baseline.events.BluetoothEvent;
import com.platypii.baseline.util.Convert;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    public static class GeneralPreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {

        private FirebaseAnalytics firebaseAnalytics;

        private CheckBoxPreference metricPreference;
        private SwitchPreference bluetoothPreference;
        private BluetoothDevicePreference bluetoothDevicePreference;
        private Preference sensorInfoPreference;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_general);
            setHasOptionsMenu(true);

            firebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

            metricPreference = (CheckBoxPreference) findPreference("metric_enabled");
            metricPreference.setOnPreferenceChangeListener(this);

            bluetoothPreference = (SwitchPreference) findPreference("bluetooth_enabled");
            bluetoothPreference.setOnPreferenceChangeListener(this);
            bluetoothDevicePreference = (BluetoothDevicePreference) findPreference("bluetooth_device_id");
            bluetoothDevicePreference.setOnPreferenceChangeListener(this);

            sensorInfoPreference = findPreference("sensor_info");
            sensorInfoPreference.setOnPreferenceClickListener(this);

            updateViews();
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            switch(preference.getKey()) {
                case "metric_enabled":
                    Convert.metric = (Boolean) value;
                    Log.i(TAG, "Setting metric mode: " + Convert.metric);
                    break;
                case "bluetooth_enabled":
                    BluetoothService.preferenceEnabled = (Boolean) value;
                    if(BluetoothService.preferenceEnabled) {
                        firebaseAnalytics.logEvent("bluetooth_enabled", null);
                        BluetoothService.startAsync(this.getActivity());
                    } else {
                        firebaseAnalytics.logEvent("bluetooth_disabled", null);
                        BluetoothService.stop();
                    }
                    break;
                case "bluetooth_device_id":
                    BluetoothService.preferenceDeviceId = (String) value;
                    BluetoothService.preferenceDeviceName = bluetoothDevicePreference.getName(BluetoothService.preferenceDeviceId);
                    Log.i(TAG, "Bluetooth device selected: " + BluetoothService.preferenceDeviceId);
                    BluetoothService.restart(this.getActivity());
                    // Save name preference
                    final SharedPreferences prefs2 = preference.getSharedPreferences();
                    final SharedPreferences.Editor edit2 = prefs2.edit();
                    edit2.putString("bluetooth_device_name", BluetoothService.preferenceDeviceName);
                    edit2.apply();
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
                final String bluetoothStatus;
                if(BluetoothService.isHardwareEnabled()) {
                    final String[] bluetoothStatusMessage = {
                            "Bluetooth stopped",
                            "Bluetooth connecting",
                            "Bluetooth connected",
                            "Bluetooth disconnected",
                            "Bluetooth shutting down"
                    };
                    bluetoothStatus = bluetoothStatusMessage[BluetoothService.getState()];
                    bluetoothDevicePreference.setEnabled(true);
                } else {
                    bluetoothStatus = "Bluetooth hardware disabled";
                    bluetoothDevicePreference.setEnabled(false);
                }
                bluetoothPreference.setSummary(bluetoothStatus);
            } else {
                bluetoothPreference.setSummary(R.string.pref_bluetooth_disabled);
                bluetoothDevicePreference.setEnabled(false);
            }
            if(BluetoothService.preferenceDeviceName != null) {
                bluetoothDevicePreference.setSummary(BluetoothService.preferenceDeviceName);
            } else if(BluetoothService.preferenceDeviceId != null) {
                bluetoothDevicePreference.setSummary(BluetoothService.preferenceDeviceId);
            } else {
                bluetoothDevicePreference.setSummary(R.string.pref_bluetooth_device_description);
            }
        }

        @Override
        public boolean onPreferenceClick(Preference preference) {
            if(preference.getKey().equals("sensor_info")) {
                // Open sensor activity
                firebaseAnalytics.logEvent("click_sensors", null);
                startActivity(new Intent(getActivity(), SensorActivity.class));
            }
            return false;
        }

        @Override
        public void onResume() {
            super.onResume();
            // Listen for bluetooth updates
            EventBus.getDefault().register(this);
        }
        @Override
        public void onPause() {
            super.onPause();
            EventBus.getDefault().unregister(this);
        }
        @Subscribe(threadMode = ThreadMode.MAIN)
        public void onBluetoothEvent(BluetoothEvent event) {
            updateViews();
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
