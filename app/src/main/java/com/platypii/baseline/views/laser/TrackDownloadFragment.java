package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.cloud.DownloadTask;
import com.platypii.baseline.events.DownloadEvent;
import com.platypii.baseline.laser.LaserLayers;
import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.charts.layers.ProfileLayer;
import com.platypii.baseline.views.charts.layers.TrackProfileLayerRemote;
import com.platypii.baseline.views.tracks.TrackLoader;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TrackDownloadFragment extends Fragment {

    private CloudData track;
    private ProgressBar downloadProgress;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.track_download, container, false);
        downloadProgress = view.findViewById(R.id.downloadProgress);

        // Load track from arguments
        try {
            final CloudData track = TrackLoader.loadTrack(getArguments());
            this.track = track;
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
            // Track download success, add to chart
            final LaserActivity laserActivity = (LaserActivity) getActivity();
            if (laserActivity != null) {
                final ProfileLayer layer = new TrackProfileLayerRemote(track, new TrackData(event.trackFile));
                LaserLayers.getInstance().add(layer);
                // Return to main fragment
                final FragmentManager fm = getFragmentManager();
                if (fm != null) fm.popBackStack();
            }
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onDownloadFailure(@NonNull DownloadEvent.DownloadFailure event) {
        if (event.track_id.equals(track.track_id)) {
            Toast.makeText(getContext(), "Track download failed", Toast.LENGTH_LONG).show();
            // Return to main fragment
            final FragmentManager fm = getFragmentManager();
            if (fm != null) fm.popBackStack();
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
