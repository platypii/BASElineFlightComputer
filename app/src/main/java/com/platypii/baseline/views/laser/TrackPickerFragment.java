package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.laser.LaserLayers;
import com.platypii.baseline.tracks.TrackData;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.views.charts.layers.Colors;
import com.platypii.baseline.views.charts.layers.ProfileLayer;
import com.platypii.baseline.views.charts.layers.TrackProfileLayer;
import com.platypii.baseline.views.charts.layers.TrackProfileLayerRemote;
import com.platypii.baseline.views.tracks.TrackListFragment;
import com.platypii.baseline.views.tracks.TrackListItem;
import com.platypii.baseline.views.tracks.TrackLoader;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.widget.AdapterView;
import java.io.File;

import static com.platypii.baseline.views.tracks.TrackListItem.*;

public class TrackPickerFragment extends TrackListFragment {
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final TrackListItem item = listAdapter.getItem(position);
        switch (item.getType()) {
            case TYPE_TRACK_LOCAL:
                final TrackFile trackFile = ((ListTrackFile) item).track;
                final TrackProfileLayer trackLayer = new TrackProfileLayer(trackFile.getName(), new TrackData(trackFile.file), Colors.nextColor());
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
        final Fragment frag = new TrackDownloadFragment();
        frag.setArguments(TrackLoader.trackBundle(track));
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.laserPanel, frag)
                .commit();
    }

    private void addLayer(@NonNull ProfileLayer layer) {
        LaserLayers.getInstance().add(layer);
        // Return to main fragment
        final FragmentManager fm = getFragmentManager();
        if (fm != null) fm.popBackStack();
    }
}
