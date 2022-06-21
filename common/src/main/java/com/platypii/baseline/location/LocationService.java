package com.platypii.baseline.location;

import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.bluetooth.BleService;
import com.platypii.baseline.bluetooth.BluetoothService;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.PubSub.Subscriber;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

/**
 * Meta location provider that uses bluetooth or android location source
 */
public class LocationService extends LocationProvider implements Subscriber<MLocation> {
    private static final String TAG = "LocationService";

    // What data source to pull from
    private static final int LOCATION_NONE = 0;
    private static final int LOCATION_ANDROID = 1;
    private static final int LOCATION_BLUETOOTH = 2;
    private static final int LOCATION_BLE = 4;
    private int locationMode = LOCATION_NONE;

    @NonNull
    private final BluetoothService bluetooth;

    // LocationService owns the alti, because it solved the circular dependency problem
    public final MyAltimeter alti = new MyAltimeter(this);

    @NonNull
    private final LocationProviderAndroid locationProviderAndroid;
    @NonNull
    public final LocationProviderBluetooth locationProviderBluetooth;
    @NonNull
    private final LocationProviderBLE locationProviderBLE;

    public LocationService(@NonNull BluetoothService bluetooth, @NonNull BleService bleService) {
        this.bluetooth = bluetooth;
        locationProviderAndroid = new LocationProviderAndroid(alti);
        locationProviderBluetooth = new LocationProviderBluetooth(alti, bluetooth);
        locationProviderBLE = new LocationProviderBLE(bleService);
    }


    @Override
    public void apply(MLocation loc) {
        // Re-post location update
        updateLocation(loc);
    }

    @NonNull
    @Override
    protected String providerName() {
        return TAG;
    }

    @NonNull
    @Override
    public String dataSource() {
        // TODO: Baro?
        if ((locationMode & LOCATION_ANDROID) > 0) {
            return Build.MANUFACTURER + " " + Build.MODEL;
        } else if ((locationMode & LOCATION_BLUETOOTH) > 0) {
            return locationProviderBluetooth.dataSource();
        } else if ((locationMode & LOCATION_BLE) > 0) {
            return locationProviderBLE.dataSource();
        } else {
            return "None";
        }
    }

    @Override
    public void start(@NonNull Context context) {
        if (locationMode != LOCATION_NONE) {
            Log.e(TAG, "Location service already started");
        }
        if (bluetooth.preferences.preferenceEnabled) {
            // Start bluetooth location service
            locationMode |= LOCATION_BLUETOOTH;
            locationProviderBluetooth.start(context);
            locationProviderBluetooth.locationUpdates.subscribe(this);

            // TODO: Do we need to do anything special if both normal and BLE sources are available?
            locationMode |= LOCATION_BLE;
            locationProviderBLE.start(context);
            locationProviderBLE.locationUpdates.subscribe(this);
            // TODO: subscribe to other updates
        } else {
            // Start android location service
            locationMode |= LOCATION_ANDROID;
            locationProviderAndroid.start(context);
            locationProviderAndroid.locationUpdates.subscribe(this);
        }
    }

    @Override
    public long lastFixDuration() {
        if (bluetooth.preferences.preferenceEnabled) {
            return locationProviderBluetooth.lastFixDuration();
        } else {
            return locationProviderAndroid.lastFixDuration();
        }
    }

    public float refreshRate() {
        if (bluetooth.preferences.preferenceEnabled) {
            return locationProviderBluetooth.refreshRate.refreshRate;
        } else {
            return locationProviderAndroid.refreshRate.refreshRate;
        }
    }

    public void permissionGranted(@NonNull Context context) {
        if ((locationMode & LOCATION_BLUETOOTH) > 0) {
            locationProviderBluetooth.start(context);
        }
        if ((locationMode & LOCATION_BLE) > 0) {
            locationProviderBLE.start(context);
        }
        if ((locationMode & LOCATION_ANDROID) > 0) {
            locationProviderAndroid.start(context);
        }
    }

    /**
     * Stops and then starts location services, such as when switch bluetooth on or off.
     */
    public synchronized void restart(@NonNull Context context) {
        Log.i(TAG, "Restarting location service");
        stop();
        start(context);
    }

    @Override
    public void stop() {
        if ((locationMode & LOCATION_ANDROID) > 0) {
            // Stop android location service
            locationProviderAndroid.locationUpdates.unsubscribe(this);
            locationProviderAndroid.stop();
            locationMode &= ~LOCATION_ANDROID;
        }
        if ((locationMode & LOCATION_BLUETOOTH) > 0) {
            // Stop bluetooth location service
            locationProviderBluetooth.locationUpdates.unsubscribe(this);
            locationProviderBluetooth.stop();
            locationMode &= ~LOCATION_BLUETOOTH;
        }
        if ((locationMode & LOCATION_BLE) > 0) {
            // Stop BLE location service
            locationProviderBLE.locationUpdates.unsubscribe(this);
            locationProviderBLE.stop();
            locationMode &= ~LOCATION_BLE;
        }
        locationMode = LOCATION_NONE;
        super.stop();
    }

}
