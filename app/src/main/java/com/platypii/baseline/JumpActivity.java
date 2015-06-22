package com.platypii.baseline;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.platypii.baseline.data.CloudData;
import com.platypii.baseline.data.Jump;
import com.platypii.baseline.data.JumpLog;
import com.platypii.baseline.data.SyncStatus;
import com.platypii.baseline.data.TheCloud;

import java.io.File;

public class JumpActivity extends Activity implements SyncStatus.SyncListener {
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

    private void updateViews() {
        if(jump != null) {
            // Find views
            final TextView filenameLabel = (TextView) findViewById(R.id.filename);
            final TextView filesizeLabel = (TextView) findViewById(R.id.filesize);
            final Button openButton = (Button) findViewById(R.id.openButton);
            final Button mapButton = (Button) findViewById(R.id.mapButton);
            final Button shareButton = (Button) findViewById(R.id.shareButton);

            filenameLabel.setText(jump.getName());
            filesizeLabel.setText(jump.getSize());

            final CloudData cloudData = jump.getCloudData();
            if(cloudData != null) {
                openButton.setText("Open");
                openButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.browser, 0, 0);
                mapButton.setEnabled(true);
                shareButton.setEnabled(true);
            } else {
                openButton.setText("Sync");
                openButton.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.upload_cloud, 0, 0);
                mapButton.setEnabled(false);
                shareButton.setEnabled(false);
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
            GoogleAuth.getAuthToken(this, new Callback<String>() {
                @Override
                public void apply(String authToken) {
                    Toast.makeText(JumpActivity.this, "Syncing track...", Toast.LENGTH_SHORT).show();
                    TheCloud.upload(jump, authToken, new Callback<Try<CloudData>>() {
                        @Override
                        public void apply(Try<CloudData> result) {
                            if(result instanceof Try.Success) {
                                updateViews();
                                Toast.makeText(JumpActivity.this, "Track sync success", Toast.LENGTH_LONG).show();
                            } else {
                                final Try.Failure<CloudData> failure = (Try.Failure<CloudData>) result;
                                Log.e(TAG, "Failed to upload track: " + failure);
                                Toast.makeText(JumpActivity.this, "Track sync failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
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
        new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("Delete Jump")
            .setMessage("Delete this track?")
            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Delete jump
                    if(jump.getCloudData() != null) {
                        // Delete from server
                        GoogleAuth.getAuthToken(JumpActivity.this, new Callback<String>() {
                            @Override
                            public void apply(String authToken) {
                                TheCloud.delete(jump, authToken, new Callback<Boolean>() {
                                    @Override
                                    public void apply(Boolean success) {
                                        if(success) {
                                            // Delete locally
                                            deleteLocal();
                                        } else {
                                            // Delete failed
                                            Toast.makeText(getApplicationContext(), "Delete failed " + jump.getName(), Toast.LENGTH_LONG).show();
                                        }
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

                public void clickShare(View v) {
                    Intents.shareTrack(this, jump);
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
