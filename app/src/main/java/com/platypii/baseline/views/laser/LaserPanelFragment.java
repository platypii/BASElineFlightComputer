package com.platypii.baseline.views.laser;

import com.platypii.baseline.Intents;
import com.platypii.baseline.R;
import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.events.ProfileLayerEvent;
import com.platypii.baseline.laser.LaserLayers;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.charts.layers.LaserProfileLayer;
import com.platypii.baseline.views.charts.layers.TrackProfileLayerLocal;
import com.platypii.baseline.views.charts.layers.TrackProfileLayerRemote;
import com.platypii.baseline.views.tracks.TrackListFragment;
import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.ListFragment;
import com.google.firebase.analytics.FirebaseAnalytics;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class LaserPanelFragment extends ListFragment {
    private final FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

    @Nullable
    private ProfileAdapter listAdapter;
    private final LaserLayers layers = LaserLayers.getInstance();
    private View helpText;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.laser_panel, container, false);
        view.findViewById(R.id.chooseTrack).setOnClickListener(this::chooseTrack);
        view.findViewById(R.id.chooseLaser).setOnClickListener(this::chooseLaser);
        view.findViewById(R.id.addLaser).setOnClickListener(this::clickAdd);
        helpText = view.findViewById(R.id.helpProfiles);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Initialize the ListAdapter
        final Activity laserActivity = getActivity();
        if (laserActivity != null) {
            listAdapter = new ProfileAdapter(laserActivity);
            setListAdapter(listAdapter);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (listAdapter != null) {
            listAdapter.setLayers(layers.layers);
        }
        updateViews();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onListItemClick(ListView parent, View view, int position, long id) {
        super.onListItemClick(parent, view, position, id);
        final Object item = parent.getItemAtPosition(position);
        if (item instanceof LaserProfileLayer) {
            // Open view mode
            final LaserProfileLayer layer = (LaserProfileLayer) item;
            firebaseAnalytics.logEvent("click_laser_profile", null);
            final Bundle bundle = new Bundle();
            bundle.putString(LaserViewFragment.LASER_ID, layer.laserProfile.laser_id);
            final Fragment frag = new LaserViewFragment();
            frag.setArguments(bundle);
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.laserPanel, frag)
                    .addToBackStack(null)
                    .commit();
        } else if (item instanceof TrackProfileLayerLocal) {
            // Open local track file charts
            final TrackFile track = ((TrackProfileLayerLocal) item).track;
            Intents.openCharts(getContext(), track.file);
        } else if (item instanceof TrackProfileLayerRemote) {
            // Open cloud track charts
            final CloudData track = ((TrackProfileLayerRemote) item).track;
            Intents.openCharts(getContext(), track.abbrvFile(getContext()));
        } else {
            Exceptions.report(new IllegalStateException("Unexpected list item type " + item));
        }
    }

    private void chooseTrack(View view) {
        firebaseAnalytics.logEvent("click_laser_track", null);
        final Fragment frag = new TrackPickerFragment();
        final Bundle args = new Bundle();
        args.putString(TrackListFragment.SEARCH_KEY, "Wingsuit BASE");
        frag.setArguments(args);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.laserPanel, frag)
                .addToBackStack(null)
                .commit();
    }

    private void chooseLaser(View view) {
        firebaseAnalytics.logEvent("click_laser_list", null);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.laserPanel, new LaserListFragment())
                .addToBackStack(null)
                .commit();
    }

    private void clickAdd(View view) {
        firebaseAnalytics.logEvent("click_laser_add", null);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.laserPanel, new LaserEditFragment())
                .addToBackStack(null)
                .commit();
    }

    private void updateViews() {
        if (layers.layers.isEmpty()) {
            helpText.setVisibility(View.VISIBLE);
        } else {
            helpText.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateLayers(ProfileLayerEvent event) {
        if (listAdapter != null) {
            listAdapter.setLayers(layers.layers);
        }
        updateViews();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
