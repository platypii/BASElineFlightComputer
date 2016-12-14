package com.platypii.baseline;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.data.Jump;
import com.platypii.baseline.data.JumpLog;
import com.platypii.baseline.cloud.TheCloud;
import com.platypii.baseline.events.AuthEvent;
import com.platypii.baseline.events.SyncEvent;
import com.platypii.baseline.util.Callback;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.File;

public class JumpActivity extends BaseActivity implements DialogInterface.OnClickListener {
    private static final String TAG = "Jump";

    private View signInPanel;
    private View signInButton;
    private View signInSpinner;
    private AlertDialog alertDialog;

    private Jump jump;

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
            final String logFilename = extras.getString("JUMP_FILE");
            final File logDir = JumpLog.getLogDirectory(getApplicationContext());
            jump = new Jump(new File(logDir, logFilename));
        }
    }

    @Override
    protected void handleSignInResult(GoogleSignInResult result) {
        super.handleSignInResult(result);
        // Update view based on sign-in state
        updateViews();
    }

    private void updateViews() {
        if(jump != null) {
            // Find views
            final TextView filenameLabel = (TextView) findViewById(R.id.filename);
            final TextView filesizeLabel = (TextView) findViewById(R.id.filesize);
            final TextView errorLabel = (TextView) findViewById(R.id.error_message);
            final Button openButton = (Button) findViewById(R.id.openButton);
            final Button mapButton = (Button) findViewById(R.id.mapButton);

            filenameLabel.setText(jump.getName());
            filesizeLabel.setText(jump.getSize());

            // Update cloud sync state
            final CloudData cloudData = jump.getCloudData();
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
                onAuthEvent(AuthEvent.SIGNED_IN);
            } else {
                openButton.setEnabled(false);
                onAuthEvent(AuthEvent.SIGNED_OUT);
            }
        }
    }

    public void clickOpen(View v) {
        final CloudData cloudData = jump.getCloudData();
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
                    Toast.makeText(JumpActivity.this, "Syncing track...", Toast.LENGTH_SHORT).show();
                    TheCloud.upload(jump, authToken, new Callback<CloudData>() {
                        @Override
                        public void apply(CloudData cloudData) {
                            updateViews();
                            Toast.makeText(JumpActivity.this, "Track sync success", Toast.LENGTH_LONG).show();
                        }
                        @Override
                        public void error(String error) {
                            Log.e(TAG, "Failed to upload track: " + error);
                            Toast.makeText(JumpActivity.this, "Track sync failed", Toast.LENGTH_LONG).show();
                        }
                    });
                }
                @Override
                public void error(String error) {
                    Toast.makeText(JumpActivity.this, "Failed to get auth token", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    public void clickKml(View v) {
        firebaseAnalytics.logEvent("click_track_kml", null);
        final CloudData cloudData = jump.getCloudData();
        if(cloudData != null) {
            // Open web app
            Intents.openTrackKml(this, cloudData);
        } else {
            Toast.makeText(getApplicationContext(), "Track not synced", Toast.LENGTH_SHORT).show();
        }
    }

    public void clickDelete(View v) {
        firebaseAnalytics.logEvent("click_track_delete_1", null);
        final int deleteConfirmMessage = (jump.getCloudData() == null)? R.string.delete_local : R.string.delete_remote;
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
        if(jump.delete()) {
            // Notify user
            Toast.makeText(getApplicationContext(), "Deleted " + jump.getName(), Toast.LENGTH_LONG).show();
            // Exit activity
            finish();
        } else {
            // Delete failed
            Toast.makeText(getApplicationContext(), "Failed to delete track " + jump.getName(), Toast.LENGTH_LONG).show();
        }
    }

    public void clickExport(View v) {
        firebaseAnalytics.logEvent("click_track_export", null);
        Intents.exportTrackFile(this, jump);
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
        if(event == AuthEvent.SIGNED_OUT) {
            signInButton.setEnabled(true);
            signInSpinner.setVisibility(View.GONE);
            signInPanel.setVisibility(View.VISIBLE);
        } else if(event == AuthEvent.SIGNING_IN) {
            signInButton.setEnabled(false);
            signInSpinner.setVisibility(View.VISIBLE);
            signInPanel.setVisibility(View.VISIBLE);
        } else if(event == AuthEvent.SIGNED_IN) {
            signInPanel.setVisibility(View.GONE);
        }
    }
}
