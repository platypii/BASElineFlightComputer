package com.platypii.baseline.location;

import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.bluetooth.BluetoothService;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Exceptions;
import android.content.Context;
import android.support.annotation.NonNull;

/**
 * Meta location provider that uses bluetooth, nmea, or android location source
 */
public class LocationService extends LocationProvider {
    private static final String TAG = "LocationService";

    private final BluetoothService bluetooth;

    // LocationService owns the alti, because it solved the circular dependency problem
    public final MyAltimeter alti = new MyAltimeter(this);

    private final LocationProviderNMEA locationProviderNMEA;
    private final LocationProviderAndroid locationProviderAndroid;
    private final LocationProviderBluetooth locationProviderBluetooth;

    public LocationService(BluetoothService bluetooth) {
        this.bluetooth = bluetooth;
        locationProviderNMEA = new LocationProviderNMEA(alti);
        locationProviderAndroid = new LocationProviderAndroid(alti);
        locationProviderBluetooth = new LocationProviderBluetooth(alti, bluetooth);
    }

    private final MyLocationListener nmeaListener = new MyLocationListener() {
        @Override
        public void onLocationChanged(@NonNull MLocation loc) {
            if(!bluetooth.preferenceEnabled) {
                updateLocation(loc);
            }
        }
        @Override
        public void onLocationChangedPostExecute() {}
    };
    private final MyLocationListener androidListener = new MyLocationListener() {
        private int overrideCount = 0;
        @Override
        public void onLocationChanged(@NonNull MLocation loc) {
            // Only use android location if we aren't getting NMEA
            // TODO: Remove the android location listener if every phone provides NMEA
            if(!bluetooth.preferenceEnabled && !locationProviderNMEA.nmeaReceived) {
                // Log on powers of 2
                overrideCount++;
                if(overrideCount > 1 && isPower2(overrideCount)) {
                    Exceptions.report(new IllegalStateException("No NMEA data, falling back to android loc #"+overrideCount+": " + loc));
                }
                updateLocation(loc);
            }
        }
        private boolean isPower2(int n) {
            return (n & (n - 1)) == 0;
        }
        @Override
        public void onLocationChangedPostExecute() {}
    };
    private final MyLocationListener bluetoothListener = new MyLocationListener() {
        @Override
        public void onLocationChanged(@NonNull MLocation loc) {
            if(bluetooth.preferenceEnabled) {
                updateLocation(loc);
            }
        }
        @Override
        public void onLocationChangedPostExecute() {}
    };

    @Override
    protected String providerName() {
        return TAG;
    }

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
