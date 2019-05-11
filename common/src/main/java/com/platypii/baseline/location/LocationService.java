package com.platypii.baseline.location;

import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.bluetooth.BluetoothService;
import com.platypii.baseline.measurements.MLocation;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

/**
 * Meta location provider that uses bluetooth, nmea, or android location source
 */
public class LocationService extends LocationProvider {
    private static final String TAG = "LocationService";

    // What data source to pull from
    private static final int LOCATION_NONE = 0;
    private static final int LOCATION_ANDROID = 1;
    private static final int LOCATION_BLUETOOTH = 2;
    private int locationMode = LOCATION_NONE;

    // Set to true on first android NMEA location
    // If we are getting NMEA, we can disable android location provider
    private boolean nmeaReceived = false;

    private final BluetoothService bluetooth;

    // LocationService owns the alti, because it solved the circular dependency problem
    public final MyAltimeter alti = new MyAltimeter(this);

    @NonNull
    private final LocationProviderNMEA locationProviderNMEA;
    @NonNull
    private final LocationProviderAndroid locationProviderAndroid;
    @NonNull
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
            nmeaReceived = true;
            if (!bluetooth.preferences.preferenceEnabled) {
                updateLocation(loc);
            }
        }
    };
    private final MyLocationListener androidListener = new MyLocationListener() {
        private int overrideCount = 0;
        @Override
        public void onLocationChanged(@NonNull MLocation loc) {
            // Only use android location if we aren't getting NMEA
            // This happens on some random phones and inside the android emulator
            if (!bluetooth.preferences.preferenceEnabled && !nmeaReceived) {
                // Log on powers of 2
                overrideCount++;
                if (overrideCount > 2 && isPower2(overrideCount)) {
                    Log.w(TAG, "No NMEA data, falling back to android loc #" + overrideCount + ": " + loc);
                }
                updateLocation(loc);
            }
        }
        private boolean isPower2(int n) {
            return (n & (n - 1)) == 0;
        }
    };
    private final MyLocationListener bluetoothListener = new MyLocationListener() {
        @Override
        public void onLocationChanged(@NonNull MLocation loc) {
            if (bluetooth.preferences.preferenceEnabled) {
                updateLocation(loc);
            }
        }
    };

    @NonNull
    @Override
    protected String providerName() {
        return TAG;
    }

    @Override
    public void start(@NonNull Context context) {
        if (locationMode != LOCATION_NONE) {
            Log.e(TAG, "Location service already started");
        }
        if (bluetooth.preferences.preferenceEnabled) {
            Log.i(TAG, "Starting location service in bluetooth mode");
            locationMode = LOCATION_BLUETOOTH;
            locationProviderBluetooth.start(context);
            locationProviderBluetooth.addListener(bluetoothListener);
        } else {
            Log.i(TAG, "Starting location service in android mode");
            locationMode = LOCATION_ANDROID;
            locationProviderNMEA.start(context);
            locationProviderNMEA.addListener(nmeaListener);
            locationProviderAndroid.start(context);
            locationProviderAndroid.addListener(androidListener);
        }
    }

    /**
     * Stops and then starts location services, such as when switch bluetooth on or off.
     */
    public void restart(@NonNull Context context) {
        Log.i(TAG, "Restarting location service");
        stop();
        start(context);
    }

    @Override
    public void stop() {
        if (locationMode == LOCATION_ANDROID) {
            Log.i(TAG, "Stopping android location service");
            locationProviderNMEA.removeListener(nmeaListener);
            locationProviderNMEA.stop();
            locationProviderAndroid.removeListener(androidListener);
            locationProviderAndroid.stop();
        } else if (locationMode == LOCATION_BLUETOOTH) {
            Log.i(TAG, "Stopping bluetooth location service");
            locationProviderBluetooth.removeListener(bluetoothListener);
            locationProviderBluetooth.stop();
        }
        locationMode = LOCATION_NONE;
        super.stop();
    }

}
