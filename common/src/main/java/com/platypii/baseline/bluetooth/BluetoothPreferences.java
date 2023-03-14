package com.platypii.baseline.bluetooth;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class BluetoothPreferences {

    private static final String PREF_BT_ENABLED = "bluetooth_enabled";
    private static final String PREF_BT_DEVICE_ID = "bluetooth_id";
    private static final String PREF_BT_DEVICE_NAME = "bluetooth_name";
    private static final String PREF_BT_BLE = "bluetooth_ble";

    // Android shared preferences for bluetooth
    public boolean preferenceEnabled = false;
    @Nullable
    public String preferenceDeviceId = null;
    @Nullable
    public String preferenceDeviceName = null;
    public boolean preferenceBle = false;

    public void load(@NonNull SharedPreferences prefs) {
        preferenceEnabled = prefs.getBoolean(PREF_BT_ENABLED, preferenceEnabled);
        preferenceDeviceId = prefs.getString(PREF_BT_DEVICE_ID, preferenceDeviceId);
        preferenceDeviceName = prefs.getString(PREF_BT_DEVICE_NAME, preferenceDeviceName);
        preferenceBle = prefs.getBoolean(PREF_BT_BLE, false);
    }

    public void save(@NonNull Context context, boolean enabled, String deviceId, String deviceName, boolean ble) {
        preferenceEnabled = enabled;
        preferenceDeviceId = deviceId;
        preferenceDeviceName = deviceName;
        preferenceBle = ble;
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        final SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(PREF_BT_ENABLED, preferenceEnabled);
        edit.putString(PREF_BT_DEVICE_ID, preferenceDeviceId);
        edit.putString(PREF_BT_DEVICE_NAME, preferenceDeviceName);
        edit.putBoolean(PREF_BT_BLE, preferenceBle);
        edit.apply();
    }

}
