package com.platypii.baseline.location;

import com.platypii.baseline.Permissions;
import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.bluetooth.BluetoothService;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Numbers;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

public class LocationProviderBluetooth extends LocationProviderNMEA {
    protected final String TAG = "ProviderBluetooth";

    @NonNull
    private final BluetoothService bluetooth;

    @NonNull
    @Override
    protected String providerName() {
        return TAG;
    }

    @NonNull
    @Override
    protected String dataSource() {
        return "BT " + bluetooth.preferences.preferenceDeviceName;
    }

    LocationProviderBluetooth(@NonNull MyAltimeter alti, @NonNull BluetoothService bluetooth) {
        super(alti);
        this.bluetooth = bluetooth;
    }

    /**
     * Listen for GPPWR command
     */
    @Override
    public void apply(@NonNull NMEA nmea) {
        // Parse PWR command
        if (nmea.sentence.startsWith("$GPPWR")) {
            final String[] split = NMEA.splitNmea(nmea.sentence);
            bluetooth.powerLevel = NMEA.parsePowerLevel(split);
            bluetooth.charging = Numbers.parseInt(split[5], 0) == 1;
        } else if (nmea.sentence.startsWith("$")) {
            super.apply(nmea);
        } else {
            Log.w(TAG, "Unexpected bluetooth message: " + nmea);
        }
    }

    /**
     * Start location updates
     *
     * @param context The Application context
     */
    @Override
    public void start(@NonNull Context context) throws SecurityException {
        Log.i(TAG, "Starting bluetooth location service " + bluetooth.getDeviceName());
        if (!Permissions.hasBluetoothPermissions(context)) {
            Log.w(TAG, "Bluetooth permissions required");
        }
        // Start NMEA updates
        bluetooth.nmeaUpdates.subscribe(this);
        bluetooth.locationUpdates.subscribe(this::onLocationUpdate);
    }

    private void onLocationUpdate(MLocation loc) {
        updateLocation(loc);
    }

    @Override
    public void stop() {
        Log.i(TAG, "Stopping bluetooth location service");
        super.stop();
        bluetooth.nmeaUpdates.unsubscribe(this);
    }
}
