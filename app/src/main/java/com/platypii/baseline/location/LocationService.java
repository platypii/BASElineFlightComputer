package com.platypii.baseline.location;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.platypii.baseline.bluetooth.BluetoothService;
import com.platypii.baseline.data.measurements.MLocation;

/**
 * Meta location provider that uses bluetooth, nmea, or android location source
 */
public class LocationService extends LocationProvider {
    private static final String TAG = "LocationService";

    // hAcc comes from android location provider
    private float hAcc = Float.NaN;

    private final LocationProviderNMEA locationProviderNMEA = new LocationProviderNMEA();
    private final LocationProviderAndroid locationProviderAndroid = new LocationProviderAndroid();
    private final LocationProviderBluetooth locationProviderBluetooth = new LocationProviderBluetooth();

    private final MyLocationListener nmeaListener = new MyLocationListener() {
        @Override
        public void onLocationChanged(MLocation loc) {
            if(!BluetoothService.preferenceEnabled) {
                if (Float.isNaN(loc.hAcc)) {
                    loc.hAcc = hAcc;
                }
                updateLocation(loc);
            }
        }
        @Override
        public void onLocationChangedPostExecute() {}
    };
    private final MyLocationListener androidListener = new MyLocationListener() {
        @Override
        public void onLocationChanged(MLocation loc) {
            // Only use android location if we aren't getting NMEA
            if(!locationProviderNMEA.nmeaReceived) {
                Log.v(TAG, "No NMEA data, falling back to LocationManager: " + loc);
                updateLocation(loc);
            }
            hAcc = loc.hAcc;
        }
        @Override
        public void onLocationChangedPostExecute() {}
    };
    private final MyLocationListener bluetoothListener = new MyLocationListener() {
        @Override
        public void onLocationChanged(MLocation loc) {
            if(BluetoothService.preferenceEnabled) {
                if (Float.isNaN(loc.hAcc)) {
                    loc.hAcc = hAcc;
                }
                updateLocation(loc);
            }
        }
        @Override
        public void onLocationChangedPostExecute() {}
    };

    @Override
    public void start(@NonNull Context context) {
        locationProviderNMEA.start(context);
        locationProviderNMEA.addListener(nmeaListener);
        locationProviderAndroid.start(context);
        locationProviderAndroid.addListener(androidListener);
        locationProviderBluetooth.start(context);
        locationProviderBluetooth.addListener(bluetoothListener);
    }

    @Override
    public void stop() {
        super.stop();
        locationProviderNMEA.removeListener(nmeaListener);
        locationProviderNMEA.stop();
        locationProviderAndroid.removeListener(androidListener);
        locationProviderAndroid.stop();
        locationProviderBluetooth.removeListener(bluetoothListener);
        locationProviderBluetooth.stop();
    }

}
