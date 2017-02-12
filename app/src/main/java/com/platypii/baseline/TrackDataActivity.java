package com.platypii.baseline;

import com.platypii.baseline.cloud.TheCloud;
import com.platypii.baseline.events.AuthEvent;
import com.platypii.baseline.tracks.TrackData;
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

    private TrackData track;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);

        // Load jump from extras
        final Bundle extras = getIntent().getExtras();
        if(extras != null && extras.getString(EXTRA_TRACK_ID) != null) {
            final String track_id = extras.getString(EXTRA_TRACK_ID);
            track = TheCloud.getCached(track_id);
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
        // Open web app
        firebaseAnalytics.logEvent("click_track_open", null);
        Intents.openTrackUrl(this, track.track_url);
    }

    public void clickKml(View v) {
        firebaseAnalytics.logEvent("click_track_kml", null);
        // Open web app
        Intents.openTrackKml(this, track.track_kml);
    }

    public void clickDelete(View v) {
        firebaseAnalytics.logEvent("click_track_delete_1", null);
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
            // Delete jump
            firebaseAnalytics.logEvent("click_track_delete_2", null);
            deleteRemote();
        }
    }

    private void deleteRemote() {
        if(TheCloud.deleteTrack(track)) {
            // Notify user
            Toast.makeText(getApplicationContext(), "Deleted track", Toast.LENGTH_LONG).show();
            // Exit activity
            finish();
        } else {
            // Delete failed
            Toast.makeText(getApplicationContext(), "Failed to delete track", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        // Listen for sync and auth updates
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        // Dismiss alert to prevent context leak
        if(alertDialog != null) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAuthEvent(AuthEvent event) {
        // If user gets signed out, close the track activity
        if(event == AuthEvent.SIGNED_OUT) {
            finish();
        }
    }

}
