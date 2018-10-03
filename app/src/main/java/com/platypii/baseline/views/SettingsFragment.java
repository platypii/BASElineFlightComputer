package com.platypii.baseline.views;

import com.platypii.baseline.Intents;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.events.AuthEvent;
import com.platypii.baseline.jarvis.AutoStop;
import com.platypii.baseline.util.Convert;
import com.platypii.baseline.views.bluetooth.BluetoothActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
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
    private Preference bluetoothPreference;
    private SwitchPreference barometerPreference;
    private Preference signInPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);
        setHasOptionsMenu(true);

        firebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());

        metricPreference = (SwitchPreference) findPreference("metric_enabled");
        metricPreference.setOnPreferenceChangeListener(this);

        barometerPreference = (SwitchPreference) findPreference("barometer_enabled");
        barometerPreference.setOnPreferenceChangeListener(this);

        findPreference("audible_settings").setOnPreferenceClickListener(this);
        bluetoothPreference = findPreference("bluetooth_settings");
        bluetoothPreference.setOnPreferenceClickListener(this);
        findPreference("sensor_info").setOnPreferenceClickListener(this);
        signInPreference = findPreference("sign_in");
        signInPreference.setOnPreferenceClickListener(this);
        findPreference("help_page").setOnPreferenceClickListener(this);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(Color.BLACK);
    }

    @Override
    public boolean onPreferenceChange(@NonNull Preference preference, Object value) {
        switch (preference.getKey()) {
            case "metric_enabled":
                Log.i(TAG, "Setting metric mode: " + value);
                Convert.metric = (Boolean) value;
                break;
            case "barometer_enabled":
                Log.i(TAG, "Setting barometer enabled: " + value);
                Services.alti.barometerEnabled = (Boolean) value;
                break;
            case "auto_stop_enabled":
                Log.i(TAG, "Setting auto-stop mode: " + value);
                AutoStop.preferenceEnabled = (Boolean) value;
                break;
        }
        updateViews();
        return true;
    }

    private void updateViews() {
        // Update metric views
        if (Convert.metric) {
            metricPreference.setSummary("Current units: metric");
        } else {
            metricPreference.setSummary("Current units: imperial");
        }

        // Update barometer views
        if (Services.alti.barometerEnabled) {
            barometerPreference.setSummary("Barometric altimeter enabled");
        } else {
            barometerPreference.setSummary("Barometric altimeter disabled");
        }

        // Update sign in views
        final SettingsActivity activity = (SettingsActivity) getActivity();
        if (BaseActivity.currentAuthState == AuthEvent.SIGNED_IN) {
            // Change to sign out state
            signInPreference.setTitle(R.string.pref_sign_out);
            final String name = activity.getDisplayName();
            // TODO: Name should never be null
            if (name != null) {
                signInPreference.setSummary(getString(R.string.pref_sign_out_description) + " " + name);
            } else {
                signInPreference.setSummary(R.string.pref_sign_out_description);
            }
        } else {
            // Change to sign in state
            signInPreference.setTitle(R.string.pref_sign_in);
            signInPreference.setSummary(R.string.pref_sign_in_description);
        }
        // Update bluetooth status
        if (Services.bluetooth.preferences.preferenceEnabled) {
            bluetoothPreference.setWidgetLayoutResource(R.layout.icon_bluetooth_on);
        } else {
            bluetoothPreference.setWidgetLayoutResource(R.layout.icon_bluetooth);
        }
        bluetoothPreference.setSummary(Services.bluetooth.getStatusMessage(activity));
    }

    @Override
    public boolean onPreferenceClick(@NonNull Preference preference) {
        if (preference.getKey().equals("audible_settings")) {
            // Open audible settings activity
            firebaseAnalytics.logEvent("click_audible_settings", null);
            startActivity(new Intent(getActivity(), AudibleSettingsActivity.class));
        } else if (preference.getKey().equals("bluetooth_settings")) {
            // Open bluetooth settings activity
            firebaseAnalytics.logEvent("click_bluetooth_settings", null);
            startActivity(new Intent(getActivity(), BluetoothActivity.class));
        } else if (preference.getKey().equals("sensor_info")) {
            // Open sensor activity
            firebaseAnalytics.logEvent("click_sensors", null);
            startActivity(new Intent(getActivity(), SensorActivity.class));
        } else if (preference.getKey().equals("sign_in")) {
            // Handle sign in click
            final BaseActivity activity = (BaseActivity) getActivity();
            // Ignore click if state is signing in
            if (BaseActivity.currentAuthState == AuthEvent.SIGNED_IN) {
                activity.clickSignOut();
            } else if (BaseActivity.currentAuthState == AuthEvent.SIGNED_OUT) {
                activity.clickSignIn();
            }
        } else if (preference.getKey().equals("help_page")) {
            // Handle help page click
            firebaseAnalytics.logEvent("click_help", null);
            Intents.openHelpUrl(getActivity());
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Listen for auth updates
        EventBus.getDefault().register(this);
        updateViews();
    }
    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAuthEvent(AuthEvent event) {
        updateViews();
    }
}
