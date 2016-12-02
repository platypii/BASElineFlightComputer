package com.platypii.baseline.location;

import android.content.Context;
import android.support.annotation.NonNull;

import com.platypii.baseline.Services;

class LocationProviderBluetooth extends LocationProviderNMEA {
    @Override
    protected String providerName() {
        return "LocationServiceBluetooth";
    }

    /**
     * Start location updates
     * @param context The Application context
     */
    @Override
    public synchronized void start(@NonNull Context context) throws SecurityException {
        // Start NMEA updates
        Services.bluetooth.addNmeaListener(this);
    }

    @Override
    public void stop() {
        super.stop();
        Services.bluetooth.removeNmeaListener(this);
    }
}
