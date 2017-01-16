package com.platypii.baseline.location;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.bluetooth.BluetoothService;
import android.util.Log;
import java.util.Locale;

/**
 * Represents the current state of GPS, including bluetooth info
 */
public class LocationStatus {
    private static final String TAG = "LocationStatus";

    public final String message;
    public final int iconColor;

    private static final int STATUS_WARNING = 0;
    private static final int STATUS_RED = 1;
    private static final int STATUS_YELLOW = 2;
    private static final int STATUS_GREEN = 3;
    private static final int STATUS_BLUE = 4;
    private static final int[] icons = {
            R.drawable.warning,
            R.drawable.status_red,
            R.drawable.status_yellow,
            R.drawable.status_green,
            R.drawable.status_blue
    };

    private LocationStatus(String message, int iconColor) {
        this.message = message;
        this.iconColor = iconColor;
    }

    public int icon() {
        return icons[iconColor];
    }

    /**
     * Get GPS status info from services
     */
    public static LocationStatus getStatus() {
        String message;
        int icon;

        // GPS signal status
        if(BluetoothService.preferenceEnabled && Services.bluetooth.getState() != BluetoothService.BT_CONNECTED) {
            // Bluetooth enabled, but not connected
            icon = STATUS_WARNING;
            switch(Services.bluetooth.getState()) {
                case BluetoothService.BT_CONNECTING:
                    message = "GPS bluetooth connecting...";
                    break;
                case BluetoothService.BT_DISCONNECTED:
                    message = "GPS bluetooth not connected";
                    break;
                default:
                    message = "GPS bluetooth not connected";
                    Log.e(TAG, "Bluetooth inconsistent state: preference enabled, state = " + Services.bluetooth.getState());
            }
        } else {
            // Internal GPS, or bluetooth connected:
            if(Services.location.lastFixDuration() < 0) {
                // No fix yet
                message = "GPS searching...";
                icon = STATUS_RED;
            } else {
                final long lastFixDuration = Services.location.lastFixDuration();
                // TODO: Use better method to determine signal.
                // Take into account acc and dop
                // How many of the last X expected fixes have we missed?
                if (lastFixDuration > 10000) {
                    message = String.format(Locale.getDefault(), "GPS last fix %ds", lastFixDuration / 1000L);
                    icon = STATUS_RED;
                } else if (lastFixDuration > 2000) {
                    message = String.format(Locale.getDefault(), "GPS last fix %ds", lastFixDuration / 1000L);
                    icon = STATUS_YELLOW;
                } else if (BluetoothService.preferenceEnabled && Services.bluetooth.getState() == BluetoothService.BT_CONNECTED) {
                    message = String.format(Locale.getDefault(), "GPS bluetooth %.2fHz", Services.location.refreshRate);
                    icon = STATUS_BLUE;
                } else {
                    message = String.format(Locale.getDefault(), "GPS %.2fHz", Services.location.refreshRate);
                    icon = STATUS_GREEN;
                }
            }
        }

        // Barometer status
        if(Services.alti.baro_sample_count == 0) {
            message += " (no barometer)";
        }

        return new LocationStatus(message, icon);
    }

}
