package com.platypii.baseline.views.tracks;

import com.platypii.baseline.Intents;
import com.platypii.baseline.R;
import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.cloud.DownloadTask;
import com.platypii.baseline.events.DownloadEvent;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.BaseActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.ProgressBar;
import android.widget.Toast;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TrackDownloadActivity extends BaseActivity {

    private CloudData track;
    private ProgressBar downloadProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.track_download);
        downloadProgress = findViewById(R.id.downloadProgress);

        // Load track from extras
        try {
            track = TrackLoader.loadTrack(getIntent().getExtras());
            // Start download
            AsyncTask.execute(new DownloadTask(this, track));
        } catch (IllegalStateException e) {
            Exceptions.report(e);
            finish();
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
