package com.platypii.baseline.location;

import com.platypii.baseline.Permissions;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.bluetooth.BluetoothState;
import com.platypii.baseline.util.StringBuilderUtil;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

/**
 * Represents the current state of GPS, including bluetooth info
 */
public class LocationStatus {
    private static final String TAG = "LocationStatus";

    public static CharSequence message;
    public static int icon;
    public static int satellites;

    // A buffer to be used for formatted strings, to avoid allocation
    private static final StringBuilder sb = new StringBuilder();

    /**
     * Get GPS status info from services
     */
    public static void updateStatus(@NonNull Context context) {
        satellites = 0;
        // Check permissions
        if (!Services.bluetooth.preferences.preferenceEnabled && !Permissions.hasFineLocationPermissions(context)) {
            icon = R.drawable.warning;
            message = "Precise location required";
            return;
        } else if (Services.bluetooth.preferences.preferenceEnabled && !Permissions.hasBluetoothConnectPermissions(context)) {
            icon = R.drawable.warning;
            message = "Bluetooth permission required";
            return;
        } else if (Services.bluetooth.preferences.preferenceEnabled && !Services.bluetooth.isEnabled()) {
            icon = R.drawable.warning;
            message = "Bluetooth disabled";
            return;
        }
        // GPS signal status
        if (Services.bluetooth.preferences.preferenceEnabled && Services.bluetooth.getState() != BluetoothState.BT_CONNECTED) {
            // Bluetooth enabled, but not connected
            icon = R.drawable.warning;
            switch (Services.bluetooth.getState()) {
                case BluetoothState.BT_STARTING:
                    message = "GPS bluetooth starting...";
                    break;
                case BluetoothState.BT_CONNECTING:
                    message = "GPS bluetooth connecting...";
                    break;
                default:
                    message = "GPS bluetooth not connected";
                    Log.e(TAG, "Bluetooth inconsistent state: preference enabled, state = " + Services.bluetooth.getState());
            }
        } else {
            // Internal GPS, or bluetooth connected:
            final long lastFixDuration = Services.location.lastFixDuration();
            if (lastFixDuration < 0) {
                // No fix yet
                message = "GPS searching...";
                icon = R.drawable.status_red;
            } else {
                // TODO: Use better method to determine signal.
                // Take into account acc and dop
                // How many of the last X expected fixes have we missed?
                if (lastFixDuration > 10000) {
                    message = "GPS last fix " + lastFixDuration / 1000L + "s";
                    icon = R.drawable.status_red;
                } else if (lastFixDuration > 1100) {
                    message = "GPS last fix " + lastFixDuration / 1000L + "s";
                    icon = R.drawable.status_yellow;
                    satellites = Services.location.lastLoc.satellitesUsed;
                } else {
                    sb.setLength(0);
                    if (Services.bluetooth.preferences.preferenceEnabled && Services.bluetooth.getState() == BluetoothState.BT_CONNECTED) {
                        sb.append("GPS bluetooth ");
                        icon = R.drawable.status_blue;
                    } else {
                        sb.append("GPS ");
                        if (Services.location.refreshRate.refreshRate < 2) {
                            // 1 hz is not enough
                            icon = R.drawable.status_yellow;
                        } else {
                            icon = R.drawable.status_green;
                        }
                    }
                    StringBuilderUtil.format2f(sb, Services.location.refreshRate.refreshRate);
                    sb.append("Hz");
                    message = sb;
                    satellites = Services.location.lastLoc.satellitesUsed;
                }
            }
        }

        // Barometer status
        if (!Services.bluetooth.preferences.preferenceEnabled && Services.alti.barometerEnabled && Services.alti.baro_sample_count == 0) {
            message = message + " (no baro)";
        }
    }

}
