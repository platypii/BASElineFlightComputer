package com.platypii.baseline;

import com.platypii.baseline.views.BaseActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

/**
 * Android permissions helpers
 */
public class Permissions {

    private static final String[] locationPermissions = { Manifest.permission.ACCESS_FINE_LOCATION };

    public static boolean hasLocationPermissions(@NonNull Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestLocationPermissions(@NonNull Activity activity) {
        ActivityCompat.requestPermissions(activity, locationPermissions, BaseActivity.RC_LOCATION);
    }

    public static boolean isLocationServiceEnabled(@NonNull Context context) {
        final LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * Open android location settings so user can enable
     */
    public static void openLocationSettings(@NonNull Context context) {
        context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

}
