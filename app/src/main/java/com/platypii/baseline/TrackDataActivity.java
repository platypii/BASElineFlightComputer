package com.platypii.baseline;

import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.events.AuthEvent;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.util.Callback;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TrackDataActivity extends BaseActivity implements DialogInterface.OnClickListener {
    private static final String TAG = "TrackActivity";

    static final String EXTRA_TRACK_ID = "TRACK_ID";

    private AlertDialog alertDialog;

    private CloudData track;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        // Load jump from extras
        final Bundle extras = getIntent().getExtras();
        if(extras != null && extras.getString(EXTRA_TRACK_ID) != null) {
            final String track_id = extras.getString(EXTRA_TRACK_ID);
            track = Services.cloud.tracks.getCached(track_id);
        } else {
            Log.e(TAG, "Failed to load track_id from extras");
        }

        // Initial view updates
        updateViews();
    }

    /**
     * Update view states (except for auth state)
     */
    private void updateViews() {
        if(track != null) {
            // Find views
            final TextView trackDate = (TextView) findViewById(R.id.trackDate);
            final TextView trackLocation = (TextView) findViewById(R.id.trackLocation);

            trackDate.setText(track.date_string);
            trackLocation.setText(track.location);
        }
    }

    public void clickOpen(View v) {
        // Analytics
        final Bundle bundle = new Bundle();
        bundle.putString("track_id", track.track_id);
        firebaseAnalytics.logEvent("click_track_open", bundle);
        // Open web app
        Intents.openTrackUrl(this, track.trackUrl);
    }

    public void clickKml(View v) {
        // Analytics
        final Bundle bundle = new Bundle();
        bundle.putString("track_id", track.track_id);
        firebaseAnalytics.logEvent("click_track_kml", bundle);
        // Open google earth
        Intents.openTrackKml(this, track.trackKml);
    }

    public void clickDelete(View v) {
        // Analytics
        final Bundle bundle = new Bundle();
        bundle.putString("track_id", track.track_id);
        firebaseAnalytics.logEvent("click_track_delete_remote_1", bundle);
        // Prompt user for confirmation
        alertDialog = new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("Delete this track?")
            .setMessage(R.string.delete_remote)
            .setPositiveButton("Delete", this)
            .setNegativeButton("Cancel", null)
            .show();
    }

    /**
     * User clicked "ok" on delete track
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if(which == DialogInterface.BUTTON_POSITIVE) {
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
        if(isSignedIn()) {
            // Begin automatic upload
            getAuthToken(new Callback<String>() {
                @Override
                public void apply(String authToken) {
                    Services.cloud.deleteTrack(track, authToken);
                }
                @Override
                public void error(String error) {
                    Toast.makeText(getApplicationContext(), "Track delete failed: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Log.w(TAG, "Track delete failed: not signed in");
        }
    }

    // Listen for deletion of this track
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeleteSuccess(SyncEvent.DeleteSuccess event) {
        if(event.track_id.equals(track.track_id)) {
            // Notify user
            Toast.makeText(getApplicationContext(), "Deleted track", Toast.LENGTH_LONG).show();
            // Exit activity
            finish();
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDeleteFailure(SyncEvent.DeleteFailure event) {
        if(event.track_id.equals(track.track_id)) {
            findViewById(R.id.deleteButton).setEnabled(true);
            // Notify user
            Toast.makeText(getApplicationContext(), "Track delete failed", Toast.LENGTH_SHORT).show();
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAuthEvent(AuthEvent event) {
        // If user gets signed out, close the track activity
        if(event == AuthEvent.SIGNED_OUT) {
            Log.i(TAG, "User signed out, closing cloud track");
            finish();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Listen for sync and auth updates
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        // Dismiss alert to prevent context leak
        if(alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

}
