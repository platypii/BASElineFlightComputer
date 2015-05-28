package com.platypii.baseline;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.platypii.baseline.data.Jump;
import com.platypii.baseline.data.JumpLog;

import java.io.File;

public class JumpActivity extends Activity {

    private Jump jump;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jump);

        // Find views
        final TextView filenameLabel = (TextView) findViewById(R.id.filename);
        final TextView filesizeLabel = (TextView) findViewById(R.id.filesize);

        // Load jump from extras
        final Bundle extras = getIntent().getExtras();
        if(extras != null) {
            final String logFilename = extras.getString("JUMP_FILE");
            final File logDir = JumpLog.getLogDirectory(getApplicationContext());
            jump = new Jump(new File(logDir, logFilename));
        }

        // Update views
        if(jump != null) {
            filenameLabel.setText(jump.getName());
            final long filesize = jump.logFile.length();
            filesizeLabel.setText(String.format("%dKiB", filesize / 1024));
        }

    }

    public void clickOpen(View v) {
        Intents.openJump(this, jump);
    }

    public void clickKml(View v) {
        Intents.openKml(this, jump);
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
                    if (jump.logFile.delete()) {
                        // Notify user
                        Toast.makeText(getApplicationContext(), "Deleted " + jump.getName(), Toast.LENGTH_LONG).show();
                        // Exit activity
                        finish();
                    } else {
                        // Delete failed
                        Toast.makeText(getApplicationContext(), "Delete failed " + jump.getName(), Toast.LENGTH_LONG).show();
                    }
                }

            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    public void clickShare(View v) {
        Intents.shareTrack(this, jump);
    }

}
