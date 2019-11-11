package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.views.charts.layers.ProfileLayer;
import com.platypii.baseline.views.charts.layers.TrackProfileLayer;
import com.platypii.baseline.views.charts.layers.TrackProfileLayerLocal;
import com.platypii.baseline.views.charts.layers.TrackProfileLayerRemote;
import com.platypii.baseline.views.tracks.TrackListFragment;
import com.platypii.baseline.views.tracks.TrackListItem;
import com.platypii.baseline.views.tracks.TrackLoader;

import android.view.View;
import android.widget.AdapterView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import java.io.File;

import static com.platypii.baseline.views.tracks.TrackListItem.ListTrackData;
import static com.platypii.baseline.views.tracks.TrackListItem.ListTrackFile;
import static com.platypii.baseline.views.tracks.TrackListItem.TYPE_TRACK_LOCAL;
import static com.platypii.baseline.views.tracks.TrackListItem.TYPE_TRACK_REMOTE;

public class TrackPickerFragment extends TrackListFragment {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final TrackListItem item = listAdapter.getItem(position);
        switch (item.getType()) {
            case TYPE_TRACK_LOCAL:
                final TrackFile trackFile = ((ListTrackFile) item).track;
                final TrackProfileLayer trackLayer = new TrackProfileLayerLocal(trackFile);
                addLayer(trackLayer);
                break;
            case TYPE_TRACK_REMOTE:
                final CloudData track = ((ListTrackData) item).track;
                // Check if track is already downloaded
                final File abbrv = track.abbrvFile(getContext());
                if (abbrv.exists()) {
                    final TrackData trackData = new TrackData(abbrv);
                    final TrackProfileLayerRemote cloudLayer = new TrackProfileLayerRemote(track, trackData);
                    addLayer(cloudLayer);
                } else {
                    // Download track file
                    downloadTrack(track);
                }
                break;
        }
    }

    private void downloadTrack(@NonNull CloudData track) {
        final TrackDownloadFragment frag = new TrackDownloadFragment();
        frag.setArguments(TrackLoader.trackBundle(track));
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.laserPanel, frag)
                .addToBackStack(null)
                .commit();
        frag.trackFile.thenAccept(trackFile -> {
            // Track download success, add to chart
            final LaserActivity laserActivity = (LaserActivity) getActivity();
            if (laserActivity != null) {
                final ProfileLayer layer = new TrackProfileLayerRemote(track, new TrackData(trackFile));
                Services.cloud.lasers.layers.add(layer);
                // Return to main fragment
                final FragmentManager fm = getFragmentManager();
                if (fm != null) fm.popBackStack();
            }
        });
        frag.trackFile.exceptionally(error -> {
            // Return to main fragment
            final FragmentManager fm = getFragmentManager();
            if (fm != null) fm.popBackStack();
            return null;
        });
    }

    private void addLayer(@NonNull ProfileLayer layer) {
        Services.cloud.lasers.layers.add(layer);
        // Return to main fragment
        final FragmentManager fm = getFragmentManager();
        if (fm != null) fm.popBackStack();
    }
}
