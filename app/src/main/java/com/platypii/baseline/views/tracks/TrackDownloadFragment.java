package com.platypii.baseline.views.tracks;

import com.platypii.baseline.R;
import com.platypii.baseline.cloud.AuthException;
import com.platypii.baseline.databinding.TrackDownloadBinding;
import com.platypii.baseline.events.SyncEvent.DownloadFailure;
import com.platypii.baseline.events.SyncEvent.DownloadProgress;
import com.platypii.baseline.events.SyncEvent.DownloadSuccess;
import com.platypii.baseline.tracks.DownloadTrackTask;
import com.platypii.baseline.tracks.TrackMetadata;
import com.platypii.baseline.util.Exceptions;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import java.io.File;
import java.io.IOException;
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

        try {
            // Load track from arguments
            track = TrackLoader.loadCloudData(getArguments());

            // Start download
            new Thread(() -> {
                final DownloadTrackTask task = new DownloadTrackTask(track);
                try {
                    task.run(getContext());
                } catch (AuthException e) {
                    Log.e(TAG, "Failed to download track auth error", e);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to download track", e);
                    Exceptions.report(e);
                }
            }).start();
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
    public void onDownloadSuccess(@NonNull DownloadSuccess event) {
        if (event.track.equals(track)) {
            // Track download success
            trackFile.complete(event.trackFile);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadFailure(@NonNull DownloadFailure event) {
        if (event.track.equals(track)) {
            Log.w(TAG, "Track download failed " + event);
            binding.downloadProgress.setVisibility(View.GONE);
            binding.downloadStatus.setText(R.string.download_failed);
            trackFile.completeExceptionally(event.error);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadProgress(@NonNull DownloadProgress event) {
        if (event.track.equals(track)) {
            // Update progress indicator
            binding.downloadProgress.setProgress(event.progress);
            binding.downloadProgress.setMax(event.total);
        }
    }

}
