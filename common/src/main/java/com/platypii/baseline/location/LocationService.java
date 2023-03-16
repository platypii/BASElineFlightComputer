package com.platypii.baseline.location;

import com.platypii.baseline.altimeter.MyAltimeter;
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
    private int locationMode = LOCATION_NONE;

    @NonNull
    private final BluetoothService bluetooth;

    // LocationService owns the alti, because it solved the circular dependency problem
    public final MyAltimeter alti = new MyAltimeter(this);

    @NonNull
    private final LocationProviderAndroid locationProviderAndroid;
    @NonNull
    private final LocationProviderBluetooth locationProviderBluetooth;

    public LocationService(@NonNull BluetoothService bluetooth) {
        this.bluetooth = bluetooth;
        locationProviderAndroid = new LocationProviderAndroid(alti);
        locationProviderBluetooth = new LocationProviderBluetooth(alti, bluetooth);
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
        if (locationMode == LOCATION_ANDROID) {
            return Build.MANUFACTURER + " " + Build.MODEL;
        } else if (locationMode == LOCATION_BLUETOOTH) {
            return locationProviderBluetooth.dataSource();
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
            Log.i(TAG, "Starting location service in bluetooth mode");
            locationMode = LOCATION_BLUETOOTH;
            locationProviderBluetooth.start(context);
            locationProviderBluetooth.locationUpdates.subscribe(this);
        } else {
            Log.i(TAG, "Starting location service in android mode");
            locationMode = LOCATION_ANDROID;
            locationProviderAndroid.start(context);
            locationProviderAndroid.locationUpdates.subscribe(this);
        }
    }

    public void permissionGranted(@NonNull Context context) {
        if (locationMode == LOCATION_BLUETOOTH) {
            locationProviderBluetooth.start(context);
        } else if (locationMode == LOCATION_ANDROID) {
            locationProviderAndroid.start(context);
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
            locationProviderAndroid.locationUpdates.unsubscribe(this);
            locationProviderAndroid.stop();
        } else if (locationMode == LOCATION_BLUETOOTH) {
            Log.i(TAG, "Stopping bluetooth location service");
            locationProviderBluetooth.locationUpdates.unsubscribe(this);
            locationProviderBluetooth.stop();
        }
        locationMode = LOCATION_NONE;
        super.stop();
    }

}
