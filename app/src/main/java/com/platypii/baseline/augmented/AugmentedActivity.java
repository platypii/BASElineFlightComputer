package com.platypii.baseline.augmented;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import com.platypii.baseline.R;

public class AugmentedActivity extends Activity implements SensorEventListener, LocationListener {
    private static final String TAG = "AR";

    private ExitView exitView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_augmented);

        // Find views
        exitView = (ExitView) findViewById(R.id.exitView);

        // Sensors
        final SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        final Sensor rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST);

        // Location
        final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } else {
            Log.e(TAG, "Location permission not granted");
            // TODO: call requestPermissions()
            // TODO: implement onRequestPermissionsResult()
        }
    }

    private final float[] cameraRotation = new float[9];
    private final float[] rotation = new float[9];
    private final float[] orientation = new float[3];
    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ROTATION_VECTOR:
                SensorManager.getRotationMatrixFromVector(rotation, event.values);
                SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X, SensorManager.AXIS_Z, cameraRotation);
                SensorManager.getOrientation(cameraRotation, orientation);
                exitView.update(orientation);
                break;
            default:
                Log.e("MySensorManager", "Received unexpected sensor event");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Stop sensor updates
        final SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.unregisterListener(this);
        // Stop location updates
        final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            locationManager.removeUpdates(this);
        } catch(SecurityException e) {
            Log.w(TAG, "Exception while stopping android location updates", e);
        }
    }

    @Override
    public void onLocationChanged(Location location) {

    }
    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {}
    @Override
    public void onProviderEnabled(String s) {}
    @Override
    public void onProviderDisabled(String s) {}
}
