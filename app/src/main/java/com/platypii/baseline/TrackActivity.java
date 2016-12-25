package com.platypii.baseline;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.tracks.TrackFiles;
import com.platypii.baseline.cloud.TheCloud;
import com.platypii.baseline.events.AuthEvent;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.util.Callback;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.File;

public class TrackActivity extends BaseActivity implements DialogInterface.OnClickListener {
    private static final String TAG = "TrackActivity";

    static final String EXTRA_TRACK_FILE = "TRACK_FILE";

    private View signInPanel;
    private View signInButton;
    private View signInSpinner;
    private AlertDialog alertDialog;

    private TrackFile trackFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jump);

        signInPanel = findViewById(R.id.sign_in_panel);
        signInButton = findViewById(R.id.sign_in_button);
        signInSpinner = findViewById(R.id.sign_in_spinner);

        // Load jump from extras
        final Bundle extras = getIntent().getExtras();
        if(extras != null) {
            final String extraTrackFile = extras.getString(EXTRA_TRACK_FILE);
            if(extraTrackFile != null) {
                final File trackDir = TrackFiles.getTrackDirectory(getApplicationContext());
                trackFile = new TrackFile(new File(trackDir, extraTrackFile));
            }
        }

        // Initial view updates
        updateViews();
        // Note: don't update auth views until we get a SyncEvent, since it would blink the sign in button
    }

    /**
     * Update view states (except for auth state)
     */
    private void updateViews() {
        if(trackFile != null) {
            // Find views
            final TextView filenameLabel = (TextView) findViewById(R.id.filename);
            final TextView filesizeLabel = (TextView) findViewById(R.id.filesize);
            final TextView errorLabel = (TextView) findViewById(R.id.error_message);
            final Button openButton = (Button) findViewById(R.id.openButton);
            final Button mapButton = (Button) findViewById(R.id.mapButton);

            filenameLabel.setText(trackFile.getName());
            filesizeLabel.setText(trackFile.getSize());

            // Update cloud sync state
            final CloudData cloudData = trackFile.getCloudData();
            if(cloudData != null) {
                errorLabel.setVisibility(View.GONE);
                openButton.setText(R.string.action_open);
                openButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.button_view, 0, 0);
                mapButton.setEnabled(true);
            } else {
                errorLabel.setText(R.string.error_not_uploaded);
                errorLabel.setVisibility(View.VISIBLE);
                openButton.setText(R.string.action_sync);
                openButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.button_sync, 0, 0);
                mapButton.setEnabled(false);
            }

            // Update view based on sign-in state
            if(isSignedIn() || cloudData != null) {
                openButton.setEnabled(true);
            } else {
                openButton.setEnabled(false);
            }
        }
    }

    public void clickOpen(View v) {
        final CloudData cloudData = trackFile.getCloudData();
        if(cloudData != null) {
            // Open web app
            firebaseAnalytics.logEvent("click_track_open", null);
            Intents.openTrackUrl(this, cloudData);
        } else {
            // Start upload
            firebaseAnalytics.logEvent("click_track_sync", null);
            getAuthToken(new Callback<String>() {
                @Override
                public void apply(String authToken) {
                    Toast.makeText(TrackActivity.this, "Syncing track...", Toast.LENGTH_SHORT).show();
                    TheCloud.upload(trackFile, authToken, new Callback<CloudData>() {
                        @Override
                        public void apply(CloudData cloudData) {
                            updateViews();
                            Toast.makeText(TrackActivity.this, "Track sync success", Toast.LENGTH_LONG).show();
                        }
                        @Override
                        public void error(String error) {
                            Log.e(TAG, "Failed to upload track: " + error);
                            Toast.makeText(TrackActivity.this, "Track sync failed", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                @Override
                public void error(String error) {
                    Toast.makeText(TrackActivity.this, "Failed to get auth token", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void clickKml(View v) {
        firebaseAnalytics.logEvent("click_track_kml", null);
        final CloudData cloudData = trackFile.getCloudData();
        if(cloudData != null) {
            // Open web app
            Intents.openTrackKml(this, cloudData);
        } else {
            Toast.makeText(getApplicationContext(), "Track not synced", Toast.LENGTH_SHORT).show();
        }
    }

    public void clickDelete(View v) {
        firebaseAnalytics.logEvent("click_track_delete_1", null);
        final int deleteConfirmMessage = (trackFile.getCloudData() == null)? R.string.delete_local : R.string.delete_remote;
        alertDialog = new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("Delete this track?")
            .setMessage(deleteConfirmMessage)
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
            deleteLocal();
        }
    }

    private void deleteLocal() {
        if(trackFile.delete()) {
            // Notify user
            Toast.makeText(getApplicationContext(), "Deleted " + trackFile.getName(), Toast.LENGTH_LONG).show();
            // Exit activity
            finish();
        } else {
            // Delete failed
            Toast.makeText(getApplicationContext(), "Failed to delete track " + trackFile.getName(), Toast.LENGTH_LONG).show();
        }
    }

    public void clickExport(View v) {
        firebaseAnalytics.logEvent("click_track_export", null);
        Intents.exportTrackFile(this, trackFile);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Listen for sync and auth updates
        EventBus.getDefault().register(this);
        updateViews();
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
    public void onSyncEvent(SyncEvent event) {
        updateViews();
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAuthEvent(AuthEvent event) {
        // Update sign in panel state
        if(event == AuthEvent.SIGNED_IN || trackFile.getCloudData() != null) {
            // Hide panel if track already synced
            signInPanel.setVisibility(View.GONE);
        } else if(event == AuthEvent.SIGNING_IN) {
            signInButton.setEnabled(false);
            signInSpinner.setVisibility(View.VISIBLE);
            signInPanel.setVisibility(View.VISIBLE);
        } else if(event == AuthEvent.SIGNED_OUT) {
            signInButton.setEnabled(true);
            signInSpinner.setVisibility(View.GONE);
            signInPanel.setVisibility(View.VISIBLE);
        }
        // Signing in enables the sync button, so update button views:
        updateViews();
    }

}
