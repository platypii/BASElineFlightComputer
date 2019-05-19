package com.platypii.baseline.views.tracks;

import com.platypii.baseline.Intents;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.BaseActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TrackRemoteActivity extends BaseActivity implements DialogInterface.OnClickListener {
    private static final String TAG = "TrackRemoteActivity";

    @Nullable
    private AlertDialog deleteConfirmation;

    private CloudData track;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_remote);

        // Load track from extras
        try {
            track = TrackLoader.loadTrack(getIntent().getExtras());

            // Setup button listeners
            findViewById(R.id.chartsButton).setOnClickListener(this::clickCharts);
            findViewById(R.id.mapButton).setOnClickListener(this::clickKml);
            findViewById(R.id.openButton).setOnClickListener(this::clickOpen);
            findViewById(R.id.deleteButton).setOnClickListener(this::clickDelete);
        } catch (IllegalStateException e) {
            Exceptions.report(e);
            finish();
        }
    }

    /**
     * Update view states (except for auth state)
     */
    private void updateViews() {
        if (track != null) {
            // Find views
            final TextView trackDate = findViewById(R.id.trackDate);
            final TextView trackLocation = findViewById(R.id.trackLocation);

            trackDate.setText(track.date_string);
            trackLocation.setText(track.location());
        }
    }

    private void clickOpen(View v) {
        // Analytics
        final Bundle bundle = new Bundle();
        bundle.putString("track_id", track.track_id);
        firebaseAnalytics.logEvent("click_track_open", bundle);
        // Open web app
        if (track.trackUrl != null) {
            Intents.openTrackUrl(this, track.trackUrl);
        }
    }

    private void clickKml(View v) {
        // Analytics
        final Bundle bundle = new Bundle();
        bundle.putString("track_id", track.track_id);
        firebaseAnalytics.logEvent("click_track_kml", bundle);
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
        final Bundle bundle = new Bundle();
        bundle.putString("track_id", track.track_id);
        firebaseAnalytics.logEvent("click_track_delete_remote_1", bundle);
        // Prompt user for confirmation
        deleteConfirmation = new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Delete this track?")
                .setMessage(R.string.delete_remote)
                .setPositiveButton(R.string.action_delete, this)
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void clickCharts(View v) {
        final Bundle bundle = new Bundle();
        bundle.putString("track_id", track.track_id);
        firebaseAnalytics.logEvent("click_track_remote_charts", bundle);
        // Open time chart activity
        Intents.openCharts(this, track);
    }

    /**
     * User clicked "ok" on delete track
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            Log.i(TAG, "User confirmed delete track " + track.track_id);
            // Analytics
            final Bundle bundle = new Bundle();
            bundle.putString("track_id", track.track_id);
            firebaseAnalytics.logEvent("click_track_delete_remote_2", bundle);
            // Disable delete button
            findViewById(R.id.deleteButton).setEnabled(false);
            // Delete track
            deleteRemote();
        }
    }

    private void deleteRemote() {
        // Delete on baseline server
        Services.cloud.deleteTrack(this, track);
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
