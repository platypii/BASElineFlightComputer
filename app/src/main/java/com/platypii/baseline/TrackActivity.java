package com.platypii.baseline;

import com.platypii.baseline.cloud.BaselineCloud;
import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.events.AuthEvent;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.tracks.TrackFiles;
import com.platypii.baseline.util.Callback;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
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
            final Button syncButton = (Button) findViewById(R.id.syncButton);

            filenameLabel.setText(trackFile.getName());
            filesizeLabel.setText(trackFile.getSize());

            // Update view based on sign-in state
            if(isSignedIn()) {
                syncButton.setEnabled(true);
            } else {
                syncButton.setEnabled(false);
            }
        }
    }

    public void clickSync(View v) {
        // Start upload
        firebaseAnalytics.logEvent("click_track_sync", null);
        getAuthToken(new Callback<String>() {
            @Override
            public void apply(String authToken) {
                Toast.makeText(getApplicationContext(), "Syncing track...", Toast.LENGTH_SHORT).show();
                BaselineCloud.upload(trackFile, authToken, new Callback<CloudData>() {
                    @Override
                    public void apply(CloudData cloudData) {
                        updateViews();
                        Toast.makeText(getApplicationContext(), "Track sync success", Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void error(String error) {
                        Log.e(TAG, "Failed to upload track: " + error);
                        Toast.makeText(getApplicationContext(), "Track sync failed", Toast.LENGTH_LONG).show();
                    }
                });
            }
            @Override
            public void error(String error) {
                Toast.makeText(getApplicationContext(), "Failed to get auth token", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void clickDelete(View v) {
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
        if(which == DialogInterface.BUTTON_POSITIVE) {
            // Delete track
            firebaseAnalytics.logEvent("click_track_delete_local_2", null);
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
    public void onSyncEvent(SyncEvent.UploadSuccess event) {
        if(event.trackFile.getName().equals(trackFile.getName())) {
            // Track uploaded, open TrackActivity
            Intents.openTrackDataActivity(this, event.cloudData);
            finish();
        }
        updateViews();
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onAuthEvent(AuthEvent event) {
        // Update sign in panel state
        if(event == AuthEvent.SIGNED_IN) {
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
