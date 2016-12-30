package com.platypii.baseline.augmented;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.location.MyLocationListener;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.tracks.TrackFileReader;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.BaseActivity;
import com.platypii.baseline.views.tracks.TrackLoader;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.camera.core.Camera;
import androidx.camera.view.PreviewView;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class AugmentedActivity extends BaseActivity implements SensorEventListener, MyLocationListener {
    private static final String TAG = "AR";

    private static final int REQUEST_PERMISSION_CAMERA = 1;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private AugmentedView augmentedView;
    private ProgressBar spinner;

    private List<MLocation> trackData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_augmented);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Find views
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        previewView = findViewById(R.id.camera);
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

        checkPermissions();
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

    private void checkPermissions() {
        // Check for camera permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            cameraProviderFuture.addListener(() -> {
                try {
                    final ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    Log.e(TAG, "Camera exception", e);
                }
            }, ContextCompat.getMainExecutor(this));
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
        }
        // TODO: Get camera FOV
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        final Preview preview = new Preview.Builder()
                .setTargetName("Preview")
                .build();

        final CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        final Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview);

        preview.setSurfaceProvider(previewView.createSurfaceProvider(camera.getCameraInfo()));
    }

    /**
     * Gets the track file from activity extras
     */
    @Nullable
    private File getTrackFile() {
        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            final String extraTrackFile = extras.getString(TrackLoader.EXTRA_TRACK_FILE);
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
                trackData = new TrackFileReader(trackFile).read();
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION_CAMERA) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Camera permission granted");
                // No need to start camera here; it is handled by onResume
            } else {
                Log.w(TAG, "Camera permission denied");
            }
        }
    }
    private final float[] cameraRotation = new float[9];
    private final float[] rotation = new float[9];
    private final float[] orientation = new float[3];
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotation, event.values);
            SensorManager.remapCoordinateSystem(rotation, SensorManager.AXIS_X, SensorManager.AXIS_Z, cameraRotation);
            SensorManager.getOrientation(cameraRotation, orientation);
            augmentedView.updateOrientation(orientation);
        } else {
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
