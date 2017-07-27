package com.platypii.baseline;

import com.platypii.baseline.bluetooth.BluetoothDevicePreference;
import com.platypii.baseline.events.AuthEvent;
import com.platypii.baseline.events.BluetoothEvent;
import com.platypii.baseline.jarvis.AutoStop;
import com.platypii.baseline.util.Convert;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.util.Log;
import com.google.firebase.analytics.FirebaseAnalytics;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

/**
 * This fragment shows the preferences
 */
public class SettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private static final String TAG = "Settings";

    private FirebaseAnalytics firebaseAnalytics;

    private SwitchPreference metricPreference;
    private SwitchPreference bluetoothPreference;
    private BluetoothDevicePreference bluetoothDevicePreference;
    private Preference signInPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        setHasOptionsMenu(true);

        firebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        metricPreference = (SwitchPreference) findPreference("metric_enabled");
        metricPreference.setOnPreferenceChangeListener(this);

        bluetoothPreference = (SwitchPreference) findPreference("bluetooth_enabled");
        bluetoothPreference.setOnPreferenceChangeListener(this);
        bluetoothDevicePreference = (BluetoothDevicePreference) findPreference("bluetooth_device_id");
        bluetoothDevicePreference.setOnPreferenceChangeListener(this);

        findPreference("audible_settings").setOnPreferenceClickListener(this);
        findPreference("sensor_info").setOnPreferenceClickListener(this);
        findPreference("help_page").setOnPreferenceClickListener(this);
        signInPreference = findPreference("sign_in");
        signInPreference.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceChange(@NonNull Preference preference, Object value) {
        switch(preference.getKey()) {
            case "metric_enabled":
                Log.i(TAG, "Setting metric mode: " + value);
                Convert.metric = (Boolean) value;
                break;
            case "auto_stop_enabled":
                Log.i(TAG, "Setting auto-stop mode: " + value);
                AutoStop.preferenceEnabled = (Boolean) value;
                break;
            case "bluetooth_enabled":
                Services.bluetooth.preferenceEnabled = (Boolean) value;
                if(Services.bluetooth.preferenceEnabled) {
                    firebaseAnalytics.logEvent("bluetooth_enabled", null);
                    Services.bluetooth.start(getActivity());
                } else {
                    firebaseAnalytics.logEvent("bluetooth_disabled", null);
                    Services.bluetooth.stop();
                }
                break;
            case "bluetooth_device_id":
                Services.bluetooth.preferenceDeviceId = (String) value;
                Services.bluetooth.preferenceDeviceName = bluetoothDevicePreference.getName(Services.bluetooth.preferenceDeviceId);
                Log.i(TAG, "Bluetooth device selected: " + Services.bluetooth.preferenceDeviceId);
                Services.bluetooth.restart(getActivity());
                // Save name preference
                final SharedPreferences prefs2 = preference.getSharedPreferences();
                final SharedPreferences.Editor edit2 = prefs2.edit();
                edit2.putString("bluetooth_device_name", Services.bluetooth.preferenceDeviceName);
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
        if(Services.bluetooth.preferenceEnabled) {
            bluetoothPreference.setSummary(Services.bluetooth.getStatusMessage());
            bluetoothDevicePreference.setEnabled(true);
        } else {
            bluetoothPreference.setSummary(R.string.pref_bluetooth_disabled);
            bluetoothDevicePreference.setEnabled(false);
        }
        if(Services.bluetooth.preferenceDeviceName != null) {
            bluetoothDevicePreference.setSummary(Services.bluetooth.preferenceDeviceName);
        } else if(Services.bluetooth.preferenceDeviceId != null) {
            bluetoothDevicePreference.setSummary(Services.bluetooth.preferenceDeviceId);
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
    public boolean onPreferenceClick(@NonNull Preference preference) {
        if(preference.getKey().equals("audible_settings")) {
            // Open audible settings activity
            firebaseAnalytics.logEvent("click_audible_settings", null);
            startActivity(new Intent(getActivity(), AudibleSettingsActivity.class));
        } else if(preference.getKey().equals("sensor_info")) {
            // Open sensor activity
            firebaseAnalytics.logEvent("click_sensors", null);
            startActivity(new Intent(getActivity(), SensorActivity.class));
        } else if(preference.getKey().equals("sign_in")) {
            // Handle sign in click
            final BaseActivity activity = (BaseActivity) getActivity();
            if(activity.isSignedIn()) {
                activity.clickSignOut();
            } else {
                activity.clickSignIn();
            }
        } else if(preference.getKey().equals("help_page")) {
            // Handle help page click
            firebaseAnalytics.logEvent("click_help", null);
            Intents.openHelpUrl(getActivity());
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
