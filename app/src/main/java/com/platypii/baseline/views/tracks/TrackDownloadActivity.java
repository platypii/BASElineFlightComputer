package com.platypii.baseline.views.tracks;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.cloud.DownloadTask;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.BaseActivity;
import android.os.AsyncTask;
import android.os.Bundle;

public class TrackDownloadActivity extends BaseActivity {

    public static final String EXTRA_TRACK_ID = "TRACK_ID";

    private CloudData track;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_download);

        // Load track from extras
        loadTrack();

        if (track != null) {
            // Start download
            AsyncTask.execute(new DownloadTask(this, track));

            // TODO: Listen for updates
            // TODO: Open charts activity when done
        } else {
            Exceptions.report(new IllegalStateException("Failed to load track from extras"));
            finish();
        }
    }

    private void loadTrack() {
        // Load track from extras
        final Bundle extras = getIntent().getExtras();
        if(extras != null && extras.getString(EXTRA_TRACK_ID) != null) {
            final String track_id = extras.getString(EXTRA_TRACK_ID);
            track = Services.cloud.tracks.getCached(track_id);
        }
    }

}
