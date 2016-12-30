package com.platypii.baseline.views.tracks;

import com.platypii.baseline.R;
import com.platypii.baseline.events.DownloadEvent;
import com.platypii.baseline.tracks.TrackMetadata;
import com.platypii.baseline.tracks.cloud.DownloadTask;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.tracks.TrackLoader;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import java.io.File;
import java9.util.concurrent.CompletableFuture;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TrackDownloadFragment extends Fragment {
    private static final String TAG = "TrackDownloadFrag";

    public final CompletableFuture<File> trackFile = new CompletableFuture<>();

    private TrackMetadata track;
    private ProgressBar downloadProgress;
    private TextView downloadStatus;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.track_download, container, false);
        downloadProgress = view.findViewById(R.id.download_progress);
        downloadStatus = view.findViewById(R.id.download_status);

        // Load track from arguments
        try {
            track = TrackLoader.loadCloudData(getArguments());
            // Start download
            AsyncTask.execute(new DownloadTask(getContext(), track));
        } catch (IllegalStateException e) {
            Exceptions.report(e);
        }

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadSuccess(@NonNull DownloadEvent.DownloadSuccess event) {
        if (event.track_id.equals(track.track_id)) {
            // Track download success
            trackFile.complete(event.trackFile);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadFailure(@NonNull DownloadEvent.DownloadFailure event) {
        if (event.track_id.equals(track.track_id)) {
            Log.w(TAG, "Track download failed " + event);
            downloadProgress.setVisibility(View.GONE);
            downloadStatus.setText(R.string.download_failed);
            trackFile.completeExceptionally(event.error);
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

}
