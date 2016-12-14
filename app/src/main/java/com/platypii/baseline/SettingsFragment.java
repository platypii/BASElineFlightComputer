package com.platypii.baseline;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.platypii.baseline.bluetooth.BluetoothDevicePreference;
import com.platypii.baseline.bluetooth.BluetoothService;
import com.platypii.baseline.events.AuthEvent;
import com.platypii.baseline.events.BluetoothEvent;
import com.platypii.baseline.util.Convert;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * This fragment shows the preferences
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private static final String TAG = "Settings";

    private FirebaseAnalytics firebaseAnalytics;

    private CheckBoxPreference metricPreference;
    private SwitchPreference bluetoothPreference;
    private BluetoothDevicePreference bluetoothDevicePreference;
    private Preference signInPreference;

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

        findPreference("sensor_info").setOnPreferenceClickListener(this);
        signInPreference = findPreference("sign_in");
        signInPreference.setOnPreferenceClickListener(this);
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
                    Services.bluetooth.start(getActivity());
                } else {
                    firebaseAnalytics.logEvent("bluetooth_disabled", null);
                    Services.bluetooth.stop();
                }
                break;
            case "bluetooth_device_id":
                BluetoothService.preferenceDeviceId = (String) value;
                BluetoothService.preferenceDeviceName = bluetoothDevicePreference.getName(BluetoothService.preferenceDeviceId);
                Log.i(TAG, "Bluetooth device selected: " + BluetoothService.preferenceDeviceId);
                Services.bluetooth.restart(getActivity());
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
        // Update metric views
        if(Convert.metric) {
            metricPreference.setSummary("Current units: metric");
        } else {
            metricPreference.setSummary("Current units: imperial");
        }
        // Update bluetooth views
        if(BluetoothService.preferenceEnabled) {
            bluetoothPreference.setSummary(Services.bluetooth.getStatusMessage());
            bluetoothDevicePreference.setEnabled(true);
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
        // Update sign in views
        final SettingsActivity activity = (SettingsActivity) getActivity();
        if(activity.isSignedIn()) {
            // Change to sign out state
            signInPreference.setTitle(R.string.pref_sign_out);
            final String name = activity.getDisplayName();
            signInPreference.setSummary(getString(R.string.pref_sign_out_description) + " " + name);
        } else {
            // Change to sign in state
            signInPreference.setTitle(R.string.pref_sign_in);
            signInPreference.setSummary(R.string.pref_sign_in_description);
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if(preference.getKey().equals("sensor_info")) {
            // Open sensor activity
            firebaseAnalytics.logEvent("click_sensors", null);
            startActivity(new Intent(getActivity(), SensorActivity.class));
        } else if(preference.getKey().equals("sign_in")) {
            // Handle sign in click
            final SettingsActivity activity = (SettingsActivity) getActivity();
            if(activity.isSignedIn()) {
                activity.clickSignOut();
            } else {
                activity.clickSignIn();
            }
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Listen for bluetooth and auth updates
        EventBus.getDefault().register(this);
        updateViews();
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
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAuthEvent(AuthEvent event) {
        updateViews();
    }
}
