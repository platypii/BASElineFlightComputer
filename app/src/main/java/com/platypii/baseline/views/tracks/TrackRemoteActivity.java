package com.platypii.baseline.views.tracks;

import com.platypii.baseline.Intents;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.tracks.TrackMetadata;
import com.platypii.baseline.util.ABundle;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.charts.ChartStatsFragment;
import com.platypii.baseline.views.charts.ChartsFragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import java.io.File;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TrackRemoteActivity extends TrackDataActivity implements DialogInterface.OnClickListener {
    private static final String TAG = "TrackRemoteActivity";

    @Nullable
    private AlertDialog deleteConfirmation;

    private TrackMetadata track;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_remote);

        // Load track from extras
        try {
            loadTrack();

            // Setup button listeners
            findViewById(R.id.openButton).setOnClickListener(this::clickOpen);
            findViewById(R.id.mapButton).setOnClickListener(this::clickKml);
            findViewById(R.id.augmentedButton).setOnClickListener(this::clickAugmented);
            findViewById(R.id.deleteButton).setOnClickListener(this::clickDelete);
            setupMenu();
        } catch (IllegalStateException e) {
            Exceptions.report(e);
            finish();
        }
    }

    /**
     * Check if track file exists, and download or load charts
     */
    private void loadTrack() {
        track = TrackLoader.loadCloudData(getIntent().getExtras());
        final File trackFile = track.abbrvFile(this);
        if (trackFile.exists()) {
            loadCharts(trackFile);
        } else {
            // File not downloaded to device, start TrackDownloadFragment
            final TrackDownloadFragment downloadFrag = new TrackDownloadFragment();
            downloadFrag.setArguments(TrackLoader.trackBundle(track));
            downloadFrag.trackFile.thenAccept(this::loadCharts);
//            downloadFrag.trackFile.exceptionally(error -> {
//                // TODO: Show download error
//            });
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.charts, downloadFrag)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
    }

    /**
     * Load chart fragments
     */
    private void loadCharts(@NonNull File trackFile) {
        // Load track data async
        new Thread(() -> trackData.complete(new TrackData(trackFile))).start();
        // Load fragments
        final FragmentManager fm = getSupportFragmentManager();
        final Fragment charts = new ChartsFragment();
        charts.setArguments(TrackLoader.trackBundle(trackFile));
        fm.beginTransaction()
                .replace(R.id.charts, charts)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
        final Fragment stats = new ChartStatsFragment();
        stats.setArguments(TrackLoader.trackBundle(trackFile));
        fm.beginTransaction()
                .replace(R.id.chartStats, stats)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    /**
     * Update header
     */
    private void updateViews() {
        if (track != null) {
            final TextView trackLocation = findViewById(R.id.trackLocation);
            trackLocation.setText(track.location());
        }
    }

    private void clickOpen(View v) {
        // Analytics
        firebaseAnalytics.logEvent("click_track_open", ABundle.of("track_id", track.track_id));
        // Open web app
        if (track.trackUrl != null) {
            Intents.openTrackUrl(this, track.trackUrl);
        }
    }

    private void clickKml(View v) {
        // Analytics
        firebaseAnalytics.logEvent("click_track_kml", ABundle.of("track_id", track.track_id));
        if (track != null) {
            // Open google earth
            Intents.openTrackKml(this, track.trackKml);
        } else {
            Exceptions.report(new NullPointerException("Track should not be null"));
        }
    }

    private void clickDelete(View v) {
        Log.i(TAG, "User clicked delete track " + track.track_id);
        // Analytics
        firebaseAnalytics.logEvent("click_track_delete_remote_1", ABundle.of("track_id", track.track_id));
        // Prompt user for confirmation
        deleteConfirmation = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Delete this track?")
                .setMessage(R.string.delete_remote)
                .setPositiveButton(R.string.action_delete, this)
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void clickAugmented(View v) {
        // Open time chart activity
        Intents.openAugmented(this, track);
    }

    /**
     * User clicked "ok" on delete track
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            Log.i(TAG, "User confirmed delete track " + track.track_id);
            // Analytics
            firebaseAnalytics.logEvent("click_track_delete_remote_2", ABundle.of("track_id", track.track_id));
            // Disable delete button
            findViewById(R.id.deleteButton).setEnabled(false);
            // Delete track
            deleteRemote();
        }
    }

    private void deleteRemote() {
        // Delete on baseline server
        Services.tracks.deleteTrack(this, track);
    }

    // Listen for deletion of this track
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeleteSuccess(@NonNull SyncEvent.DeleteSuccess event) {
        if (event.track_id.equals(track.track_id)) {
            // Exit activity
            finish();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeleteFailure(@NonNull SyncEvent.DeleteFailure event) {
        if (event.track_id.equals(track.track_id)) {
            findViewById(R.id.deleteButton).setEnabled(true);
            // Notify user
            Toast.makeText(getApplicationContext(), "Track delete failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSignOut(@NonNull AuthState.SignedOut event) {
        // If user gets signed out, close the track activity
        Log.i(TAG, "User signed out, closing cloud track");
        finish();
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
        if (deleteConfirmation != null) {
            deleteConfirmation.dismiss();
            deleteConfirmation = null;
        }
    }

}
