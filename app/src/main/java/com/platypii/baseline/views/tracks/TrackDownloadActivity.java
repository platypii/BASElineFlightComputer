package com.platypii.baseline.views.tracks;

import com.platypii.baseline.Intents;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.cloud.DownloadTask;
import com.platypii.baseline.events.DownloadEvent;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.BaseActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.Toast;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TrackDownloadActivity extends BaseActivity {
    private static final String TAG = "TrackDownload";

    public static final String EXTRA_TRACK_ID = "TRACK_ID";

    private CloudData track;

    private ProgressBar downloadProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_download);

        downloadProgress = findViewById(R.id.downloadProgress);

        // Load track from extras
        loadTrack();

        if (track != null) {
            // Start download
            AsyncTask.execute(new DownloadTask(this, track));
        } else {
            Exceptions.report(new IllegalStateException("Failed to load track from extras"));
            finish();
        }
    }

    private void loadTrack() {
        // Load track from extras
        final Bundle extras = getIntent().getExtras();
        if (extras != null) {
            final String track_id = extras.getString(EXTRA_TRACK_ID);
            if (track_id != null) {
                track = Services.cloud.listing.cache.getTrack(track_id);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadSuccess(@NonNull DownloadEvent.DownloadSuccess event) {
        if (event.track_id.equals(track.track_id)) {
            // Track download success, open ChartsActivity
            Intents.openCharts(this, event.trackFile);
            finish();
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadFailure(@NonNull DownloadEvent.DownloadFailure event) {
        if (event.track_id.equals(track.track_id)) {
            Log.e(TAG, "Failed to upload track: " + event.error);
            Toast.makeText(getApplicationContext(), "Track download failed", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadProgress(@NonNull DownloadEvent.DownloadProgress event) {
        if (event.track_id.equals(track.track_id)) {
            // Update progress indicator
            downloadProgress.setProgress(event.progress);
            downloadProgress.setMax(event.total);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Listen for sync and auth updates
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

}
