package com.platypii.baseline.location;

import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.bluetooth.BluetoothService;
import com.platypii.baseline.measurements.MLocation;
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

    public LocationServiceBlue(BluetoothService bluetooth) {
        this.bluetooth = bluetooth;
        locationProviderBluetooth = new LocationProviderBluetooth(alti, bluetooth);
    }

    private final MyLocationListener bluetoothListener = new MyLocationListener() {
        @Override
        public void onLocationChanged(@NonNull MLocation loc) {
            if(bluetooth.preferences.preferenceEnabled) {
                updateLocation(loc);
            }
        }
        @Override
        public void onLocationChangedPostExecute() {}
    };

    @NonNull
    @Override
    protected String providerName() {
        return TAG;
    }

    @Override
    public void start(@NonNull Context context) {
        locationProviderBluetooth.start(context);
        locationProviderBluetooth.addListener(bluetoothListener);
    }

    @Override
    public void stop() {
        super.stop();
        locationProviderBluetooth.removeListener(bluetoothListener);
        locationProviderBluetooth.stop();
    }

}
