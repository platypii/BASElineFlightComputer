package com.platypii.baseline.bluetooth;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BlePreferences {

    private static final String PREF_BLE_ENABLED = "ble_enabled";
    private static final String PREF_BLE_DEVICE_ID = "ble_id";
    private static final String PREF_BLE_DEVICE_NAME = "ble_name";

    // Android shared preferences for bluetooth
    public boolean preferenceEnabled = false;
    @Nullable
    public String preferenceDeviceId = null;
    @Nullable
    public String preferenceDeviceName = null;

    public void load(@NonNull SharedPreferences prefs) {
        preferenceEnabled = prefs.getBoolean(PREF_BLE_ENABLED, preferenceEnabled);
        preferenceDeviceId = prefs.getString(PREF_BLE_DEVICE_ID, preferenceDeviceId);
        preferenceDeviceName = prefs.getString(PREF_BLE_DEVICE_NAME, preferenceDeviceName);
    }

    public void save(@NonNull Context context, boolean enabled, String deviceId, String deviceName) {
        preferenceEnabled = enabled;
        preferenceDeviceId = deviceId;
        preferenceDeviceName = deviceName;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(PREF_BLE_ENABLED, preferenceEnabled);
        edit.putString(PREF_BLE_DEVICE_ID, preferenceDeviceId);
        edit.putString(PREF_BLE_DEVICE_NAME, preferenceDeviceName);
        edit.apply();
    }

}
