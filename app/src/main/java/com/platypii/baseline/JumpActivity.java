package com.platypii.baseline;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.platypii.baseline.data.Jump;
import com.platypii.baseline.data.JumpLog;
import com.platypii.baseline.data.TheCloud;

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

    public void clickUpload(View v) {
        TheCloud.uploadAsync(jump);
    }

    public void clickDelete(View v) {
        new AlertDialog.Builder(this)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setTitle("Delete Jump")
            .setMessage("Are you sure you want to delete this track?")
            .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Delete jump
                    jump.logFile.delete();
                    // Notify user
                    Toast.makeText(getApplicationContext(), "Deleted " + jump.getName(), Toast.LENGTH_LONG).show();
                    // Exit activity
                    finish();
                }

            })
            .setNegativeButton("Cancel", null)
            .show();
    }

//    public void clickExport(View v) {
//        // Share jump log using android share options
//        final Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_SEND);
//        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(jump.logFile));
//        intent.setType("text/plain");
//        startActivity(intent);
//    }

}
