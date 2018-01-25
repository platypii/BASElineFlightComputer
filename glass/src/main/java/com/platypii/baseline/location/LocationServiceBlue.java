package com.platypii.baseline.location;

import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.bluetooth.BluetoothService;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Meta location provider that uses only bluetooth
 */
public class LocationServiceBlue extends LocationProvider {
    private static final String TAG = "LocationService";

    private final BluetoothService bluetooth;

    // LocationService owns the alti, because it solved the circular dependency problem
    public final MyAltimeter alti = new MyAltimeter(this);

    private final LocationProviderBluetooth locationProviderBluetooth;

    @NonNull
    @Override
    protected String dataSource() {
        return locationProviderBluetooth.dataSource();
    }

    public LocationServiceBlue(BluetoothService bluetooth) {
        this.bluetooth = bluetooth;
        locationProviderBluetooth = new LocationProviderBluetooth(alti, bluetooth);
    }

    @NonNull
    @Override
    protected String providerName() {
        return TAG;
    }

    @Override
    public void start(@NonNull Context context) {
        locationProviderBluetooth.start(context);
        locationProviderBluetooth.locationUpdates.subscribe(this::updateLocation);
    }

    @Override
    public void stop() {
        super.stop();
        locationProviderBluetooth.locationUpdates.unsubscribe(this::updateLocation);
        locationProviderBluetooth.stop();
    }

}
