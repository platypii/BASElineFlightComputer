package com.platypii.baseline.location;

import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.bluetooth.BluetoothService;

import android.content.Context;
import android.support.annotation.NonNull;

class LocationProviderBluetooth extends LocationProviderNMEA {

    private final BluetoothService bluetooth;

    @Override
    protected String providerName() {
        return "LocationServiceBluetooth";
    }

    LocationProviderBluetooth(MyAltimeter alti, BluetoothService bluetooth) {
        super(alti, bluetooth);
        this.bluetooth = bluetooth;
    }

    /**
     * Start location updates
     * @param context The Application context
     */
    @Override
    public synchronized void start(@NonNull Context context) throws SecurityException {
        // Start NMEA updates
        bluetooth.addNmeaListener(this);
    }

    @Override
    public void stop() {
        super.stop();
        bluetooth.removeNmeaListener(this);
    }
}
