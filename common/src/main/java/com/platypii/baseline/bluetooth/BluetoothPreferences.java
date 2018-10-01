package com.platypii.baseline.bluetooth;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

public class BluetoothPreferences {

    private static final String PREF_BT_ENABLED = "bluetooth_enabled";
    private static final String PREF_BT_DEVICE_ID = "bluetooth_id";
    private static final String PREF_BT_DEVICE_NAME = "bluetooth_name";

    // Android shared preferences for bluetooth
    public boolean preferenceEnabled = false;
    @Nullable
    public String preferenceDeviceId = null;
    @Nullable
    public String preferenceDeviceName = null;

    public void load(Context context) {
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        preferenceEnabled = prefs.getBoolean(PREF_BT_ENABLED, preferenceEnabled);
        preferenceDeviceId = prefs.getString(PREF_BT_DEVICE_ID, preferenceDeviceId);
        preferenceDeviceName = prefs.getString(PREF_BT_DEVICE_NAME, preferenceDeviceName);
    }

    public void save(Context context, boolean enabled, String deviceId, String deviceName) {
        preferenceEnabled = enabled;
        preferenceDeviceId = deviceId;
        preferenceDeviceName = deviceName;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(PREF_BT_ENABLED, preferenceEnabled);
        edit.putString(PREF_BT_DEVICE_ID, preferenceDeviceId);
        edit.putString(PREF_BT_DEVICE_NAME, preferenceDeviceName);
        edit.apply();
    }

}
