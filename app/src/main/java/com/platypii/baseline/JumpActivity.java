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
import com.platypii.baseline.data.CloudData;
import com.platypii.baseline.data.Jump;
import com.platypii.baseline.data.JumpLog;
import com.platypii.baseline.data.SyncStatus;
import com.platypii.baseline.data.TheCloud;

import java.io.File;

public class JumpActivity extends BaseActivity implements SyncStatus.SyncListener {
    private static final String TAG = "Jump";

    private Jump jump;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jump);

        // Load jump from extras
        final Bundle extras = getIntent().getExtras();
        if(extras != null) {
            final String logFilename = extras.getString("JUMP_FILE");
            final File logDir = JumpLog.getLogDirectory(getApplicationContext());
            jump = new Jump(new File(logDir, logFilename));
        }

        // Update views
        updateViews();
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
                openButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.browser, 0, 0);
                mapButton.setEnabled(true);
            } else {
                errorLabel.setText(R.string.error_not_uploaded);
                errorLabel.setVisibility(View.VISIBLE);
                openButton.setText(R.string.action_sync);
                openButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.upload_cloud, 0, 0);
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
        final CloudData cloudData = jump.getCloudData();
        if(cloudData != null) {
            // Open web app
            Intents.openTrackUrl(this, cloudData);
        } else {
            // Start upload
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
        final CloudData cloudData = jump.getCloudData();
        if(cloudData != null) {
            // Open web app
            Intents.openTrackKml(this, cloudData);
        } else {
            Toast.makeText(getApplicationContext(), "Track not synced", Toast.LENGTH_SHORT).show();
        }
    }

    public void clickDelete(View v) {
        final int deleteConfirmMessage = (jump.getCloudData() == null)? R.string.delete_local : R.string.delete_remote;
        new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("Delete this track?")
            .setMessage(deleteConfirmMessage)
            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Delete jump
                    if(jump.getCloudData() != null) {
                        // Delete from server
                        getAuthToken(new Callback<String>() {
                            @Override
                            public void apply(String authToken) {
                                TheCloud.delete(jump, authToken, new Callback<Void>() {
                                    @Override
                                    public void apply(Void v) {
                                        // Delete locally
                                        deleteLocal();
                                    }
                                    @Override
                                    public void error(String error) {
                                        // Delete failed
                                        Toast.makeText(getApplicationContext(), "Delete failed " + jump.getName(), Toast.LENGTH_LONG).show();
                                    }
                                });

                            }
                            @Override
                            public void error(String authToken) {
                                TheCloud.delete(jump, authToken, new Callback<Void>() {
                                    @Override
                                    public void apply(Void v) {
                                        // Delete locally
                                        deleteLocal();
                                    }
                                    @Override
                                    public void error(String error) {
                                        // Delete failed
                                        Toast.makeText(getApplicationContext(), "Delete failed " + jump.getName(), Toast.LENGTH_LONG).show();
                                    }
                                });

                            }
                        });
                    } else {
                        // Not uploaded, just delete locally
                        deleteLocal();
                    }
                }

            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteLocal() {
        if(jump.delete()) {
            // Notify user
            Toast.makeText(getApplicationContext(), "Deleted " + jump.getName(), Toast.LENGTH_LONG).show();
            // Exit activity
            finish();
        } else {
            // Delete failed
            Toast.makeText(getApplicationContext(), "Delete failed " + jump.getName(), Toast.LENGTH_LONG).show();
        }
    }

    public void clickExport(View v) {
        Intents.shareTrackFile(this, jump);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Listen for sync updates
        SyncStatus.addListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Listen for sync updates
        SyncStatus.removeListener(this);
    }

    @Override
    public void syncUpdate() {
        updateViews();
    }
}
