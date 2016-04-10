package com.platypii.baseline.location;

import android.content.Context;
import android.support.annotation.NonNull;

import com.platypii.baseline.bluetooth.BluetoothService;

class LocationProviderBluetooth extends LocationProviderNMEA {
    private static final String TAG = "LocationServiceBluetooth";

    /**
     * Start location updates
     * @param context The Application context
     */
    @Override
    public synchronized void start(@NonNull Context context) throws SecurityException {
        // Start NMEA updates
        BluetoothService.addNmeaListener(this);
    }

    @Override
    public void stop() {
        super.stop();
        BluetoothService.removeNmeaListener(this);
    }
}
