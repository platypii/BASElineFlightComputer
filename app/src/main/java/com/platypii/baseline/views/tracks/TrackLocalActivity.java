package com.platypii.baseline.views.tracks;

import com.platypii.baseline.Intents;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.tracks.TrackMetadata;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.charts.ChartStatsFragment;
import com.platypii.baseline.views.charts.ChartsFragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TrackLocalActivity extends TrackDataActivity implements DialogInterface.OnClickListener {
    private static final String TAG = "TrackLocalActivity";

    private ProgressBar uploadProgress;
    private TextView alertLabel;
    private Button deleteButton;
    @Nullable
    private AlertDialog alertDialog;

    private TrackFile trackFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_local);

        final TextView filenameLabel = findViewById(R.id.filename);
        final TextView filesizeLabel = findViewById(R.id.filesize);
        uploadProgress = findViewById(R.id.uploadProgress);
        alertLabel = findViewById(R.id.alert_message);
        deleteButton = findViewById(R.id.deleteButton);

        // Load track from extras
        try {
            trackFile = TrackLoader.loadTrackFile(this);
            loadCharts();

            // Update views
            filenameLabel.setText(trackFile.getName());
            filesizeLabel.setText(trackFile.getSize());

            // Setup button listeners
            findViewById(R.id.exportButton).setOnClickListener(this::clickExport);
            findViewById(R.id.deleteButton).setOnClickListener(this::clickDelete);
            setupMenu();
        } catch (IllegalStateException e) {
            Exceptions.report(e);
            finish();
        }
    }

    /**
     * Load charts fragment
     */
    private void loadCharts() {
        // Load track data async
        new Thread(() -> trackData.complete(new TrackData(trackFile.file))).start();
        // Load fragments
        final FragmentManager fm = getSupportFragmentManager();
        final Fragment charts = new ChartsFragment();
        charts.setArguments(TrackLoader.trackBundle(trackFile.file));
        fm.beginTransaction()
                .replace(R.id.charts, charts)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
        final Fragment stats = new ChartStatsFragment();
        stats.setArguments(TrackLoader.trackBundle(trackFile.file));
        fm.beginTransaction()
                .replace(R.id.chartStats, stats)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    /**
     * Update view states (except for auth state)
     */
    private void updateViews() {
        if (trackFile != null) {
            // Check if upload completed
            final TrackMetadata cloudData = Services.tracks.store.getCloudData(trackFile);
            if (cloudData != null) {
                // Track uploaded, open TrackRemoteActivity
                Intents.openTrackRemote(this, cloudData);
                finish();
            } else if (Services.tracks.store.isUploading(trackFile)) {
                uploadProgress.setProgress(Services.tracks.store.getUploadProgress(trackFile));
                uploadProgress.setMax((int) trackFile.file.length());
                uploadProgress.setVisibility(View.VISIBLE);
                alertLabel.setText(R.string.uploading);
                alertLabel.setVisibility(View.VISIBLE);
                deleteButton.setEnabled(false);
            } else {
                // Not uploaded
                uploadProgress.setVisibility(View.GONE);
                if (AuthState.getUser() != null) {
                    alertLabel.setText(R.string.upload_waiting);
                    alertLabel.setVisibility(View.VISIBLE);
                } else {
                    alertLabel.setVisibility(View.GONE);
                }
                deleteButton.setEnabled(true);
            }
        }
    }

    private void clickDelete(View v) {
        firebaseAnalytics.logEvent("click_track_delete_local_1", null);
        alertDialog = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Delete this track?")
                .setMessage(R.string.delete_local)
                .setPositiveButton("Delete", this)
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * User clicked "ok" on delete track
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            // Delete track
            firebaseAnalytics.logEvent("click_track_delete_local_2", null);
            deleteLocal();
        }
    }

    private void deleteLocal() {
        if (Services.tracks.store.isUploading(trackFile)) {
            Toast.makeText(getApplicationContext(), "Delete failed, upload in progress", Toast.LENGTH_LONG).show();
        } else if (Services.tracks.store.delete(trackFile)) {
            // Exit activity
            finish();
        } else {
            // Delete failed
            Toast.makeText(getApplicationContext(), "Failed to delete track " + trackFile.getName(), Toast.LENGTH_LONG).show();
        }
    }

    private void clickExport(View v) {
        firebaseAnalytics.logEvent("click_track_export", null);
        Intents.exportTrackFile(this, trackFile);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUploadSuccess(@NonNull SyncEvent.UploadSuccess event) {
        if (event.trackFile.getName().equals(trackFile.getName())) {
            // Track uploaded, open TrackRemoteActivity
            Toast.makeText(getApplicationContext(), "Track sync success", Toast.LENGTH_SHORT).show();
            Intents.openTrackRemote(this, event.cloudData);
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUploadFailure(@NonNull SyncEvent.UploadFailure event) {
        if (event.trackFile.getName().equals(trackFile.getName())) {
            Log.e(TAG, "Failed to upload track: " + event.error);
            updateViews();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onUploadProgress(@NonNull SyncEvent.UploadProgress event) {
        if (event.trackFile.getName().equals(trackFile.getName())) {
            updateViews();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAuthEvent(AuthState event) {
        updateViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Listen for sync and auth updates
        EventBus.getDefault().register(this);
        updateViews();
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Dismiss alert to prevent context leak
        if (alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

}
