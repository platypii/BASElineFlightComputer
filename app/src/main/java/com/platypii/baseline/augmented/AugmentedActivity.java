package com.platypii.baseline.augmented;

import com.platypii.baseline.R;
import com.platypii.baseline.util.Callback;
import android.Manifest;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.cameraview.CameraView;
import java.util.List;

public class AugmentedActivity extends Activity implements SensorEventListener, LocationListener {
    private static final String TAG = "AR";

    public static final String EXTRA_TRACK_ID = "TRACK_ID";

    private static final int REQUEST_PERMISSION_CAMERA = 1;

    private CameraView cameraView;
    private AugmentedView augmentedView;
    private ProgressBar spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_augmented);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Find views
        cameraView = findViewById(R.id.camera);
        augmentedView = findViewById(R.id.augmentedView);
        spinner = findViewById(R.id.augmentedSpinner);

        // Get track id from extras
        final Bundle extras = getIntent().getExtras();
        if(extras != null) {
            final String track_id = extras.getString(EXTRA_TRACK_ID);
            if(track_id != null) {
                fetchGeoData(track_id);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Start orientation listener
        final SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        final Sensor rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST);

        // Start location listener
        // TODO: Use BASEline location manager
        final LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } else {
            Log.e(TAG, "Location permission not granted");
        }

    }

    /** Fetch track geo data */
    private void fetchGeoData(String track_id) {
        spinner.setVisibility(View.VISIBLE);
        new GeoDataTask(track_id, new Callback<List<Location>>() {
            @Override
            public void apply(List<Location> points) {
                Log.i(TAG, "Got geo data");
                augmentedView.updateTrackData(points);
                spinner.setVisibility(View.GONE);
            }
            @Override
            public void error(String error) {
                Log.e(TAG, "Error fetching geo data: " + error);
                Toast.makeText(AugmentedActivity.this, "Error fetching geo data: " + error, Toast.LENGTH_LONG).show();
            }
        }).execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Camera permission granted");
                    // No need to start camera here; it is handled by onResume
                } else {
                    Log.w(TAG, "Camera permission denied");
                }
            }
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
                augmentedView.updateOrientation(orientation);
                break;
            default:
                Log.e("MySensorManager", "Received unexpected sensor event");
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onLocationChanged(Location location) {
        // Update location
        augmentedView.updateLocation(location);
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    protected void onResume() {
        super.onResume();
        // Check for camera permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraView.start();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
        }
        // TODO: Get camera FOV
    }
    @Override
    protected void onPause() {
        cameraView.stop();
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
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

}
