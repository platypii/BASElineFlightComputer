package com.platypii.baseline;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;
import static com.platypii.baseline.RequestCodes.RC_BLUE_ALL;
import static com.platypii.baseline.RequestCodes.RC_LOCATION;

/**
 * Android permissions helpers
 */
public class Permissions {

    private static final String[] locationPermissions = { Manifest.permission.ACCESS_FINE_LOCATION };

    public static String[] btPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            };
        } else {
            return new String[]{ Manifest.permission.ACCESS_FINE_LOCATION };
        }
    }

    public static boolean hasLocationPermissions(@NonNull Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PERMISSION_GRANTED;
    }

    public static void requestLocationPermissions(@NonNull Activity activity) {
        ActivityCompat.requestPermissions(activity, locationPermissions, RC_LOCATION);
    }

    public static boolean isLocationServiceEnabled(@NonNull Context context) {
        final LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return lm != null && lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Open android location settings so user can enable
     */
    public static void openLocationSettings(@NonNull Context context) {
        context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    public static boolean hasBluetoothConnectPermissions(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    public static boolean hasBluetoothPermissions(@NonNull Context context) {
        return hasBluetoothConnectPermissions(context)
                && hasBluetoothScanPermissions(context)
                && hasLocationPermissions(context);
    }

    public static void requestBluetoothPermissions(@NonNull Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(activity, btPermissions(), RC_BLUE_ALL);
        }
    }

    private static boolean hasBluetoothScanPermissions(@NonNull Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PERMISSION_GRANTED;
        } else {
            return true; // TODO: check for location permission on older android?
        }
    }

}
