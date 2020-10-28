package com.platypii.baseline.views.tracks;

import com.platypii.baseline.R;
import com.platypii.baseline.databinding.TrackDownloadBinding;
import com.platypii.baseline.events.DownloadEvent;
import com.platypii.baseline.tracks.TrackMetadata;
import com.platypii.baseline.tracks.cloud.DownloadTask;
import com.platypii.baseline.util.Exceptions;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private TrackDownloadBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = TrackDownloadBinding.inflate(inflater, container, false);

        // Load track from arguments
        try {
            track = TrackLoader.loadCloudData(getArguments());
            // Start download
            AsyncTask.execute(new DownloadTask(getContext(), track));
        } catch (IllegalStateException e) {
            Exceptions.report(e);
        }

        return binding.getRoot();
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
            binding.downloadProgress.setVisibility(View.GONE);
            binding.downloadStatus.setText(R.string.download_failed);
            trackFile.completeExceptionally(event.error);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadProgress(@NonNull DownloadEvent.DownloadProgress event) {
        if (event.track_id.equals(track.track_id)) {
            // Update progress indicator
            binding.downloadProgress.setProgress(event.progress);
            binding.downloadProgress.setMax(event.total);
        }
    }

}
