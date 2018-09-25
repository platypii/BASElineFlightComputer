package com.platypii.baseline.augmented;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.tracks.TrackFileData;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.BaseActivity;
import com.platypii.baseline.views.tracks.TrackLocalActivity;
import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import com.google.android.cameraview.CameraView;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

public class AugmentedActivity extends BaseActivity implements SensorEventListener, MyLocationListener {
    private static final String TAG = "AR";

    public static final String EXTRA_TRACK_ID = "TRACK_ID";

    private static final int REQUEST_PERMISSION_CAMERA = 1;

    private CameraView cameraView;
    private AugmentedView augmentedView;
    private ProgressBar spinner;

    private List<MLocation> trackData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_augmented);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Find views
        cameraView = findViewById(R.id.camera);
        augmentedView = findViewById(R.id.augmentedView);
        spinner = findViewById(R.id.augmentedSpinner);

        // Load track from extras
        final File trackFile = getTrackFile();
        if (trackFile != null) {
            Log.i(TAG, "Loading track data");
            // Load async
            new LoadTask(trackFile, this).execute();
        } else {
            Exceptions.report(new IllegalStateException("Failed to load track file from extras"));
            // Finish activity
            finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        // Start orientation listener
        final SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            final Sensor rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_FASTEST);
        }

        // Start location listener
        Services.location.addListener(this);
    }

    /**
     * Gets the track file from activity extras
     */
    @Nullable
    private File getTrackFile() {
        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            final String extraTrackFile = extras.getString(TrackLocalActivity.EXTRA_TRACK_FILE);
            if (extraTrackFile != null) {
                return new File(extraTrackFile);
            }
        }
        return null;
    }

    private class LoadTask extends AsyncTask<Void,Void,Void> {
        private final File trackFile;
        private final WeakReference<AugmentedActivity> activityRef;

        private LoadTask(File trackFile, AugmentedActivity activity) {
            this.trackFile = trackFile;
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute() {
            spinner.setVisibility(View.VISIBLE);
        }
        @Override
        protected Void doInBackground(Void... voids) {
            final AugmentedActivity activity = activityRef.get();
            if (activity != null && !activity.isFinishing()) {
                Log.i(TAG, "Loading track data from " + trackFile);
                trackData = TrackFileData.getTrackData(trackFile);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void v) {
            final AugmentedActivity activity = activityRef.get();
            if (activity != null && !activity.isFinishing()) {
                Log.i(TAG, "Loaded track data with " + trackData.size() + " points");
                augmentedView.updateTrackData(trackData);
                spinner.setVisibility(View.GONE);
            }
        }
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
                Log.e(TAG, "Received unexpected sensor event");
        }
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onLocationChanged(@NonNull MLocation location) {
        // Update location
        augmentedView.updateLocation(location);
    }

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
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
        // Stop location updates
        Services.location.removeListener(this);
    }

}
