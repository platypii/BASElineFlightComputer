package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.cloud.DownloadTask;
import com.platypii.baseline.events.DownloadEvent;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.tracks.TrackLoader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TrackDownloadFragment extends Fragment {

    private String track_id;
    private ProgressBar downloadProgress;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.track_download, container, false);
        downloadProgress = view.findViewById(R.id.downloadProgress);

        // Load track from arguments
        try {
            final CloudData track = TrackLoader.loadTrack(getArguments());
            this.track_id = track.track_id;
            // Start download
            AsyncTask.execute(new DownloadTask(getContext(), track));
        } catch (IllegalStateException e) {
            Exceptions.report(e);
        }

        return view;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadProgress(@NonNull DownloadEvent.DownloadProgress event) {
        if (event.track_id.equals(track_id)) {
            // Update progress indicator
            downloadProgress.setProgress(event.progress);
            downloadProgress.setMax(event.total);
        }
    }

}
