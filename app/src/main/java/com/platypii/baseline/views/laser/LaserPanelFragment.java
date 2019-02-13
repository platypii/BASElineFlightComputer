package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.events.ProfileLayerEvent;
import com.platypii.baseline.laser.LaserLayers;
import com.platypii.baseline.views.tracks.TrackListFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.firebase.analytics.FirebaseAnalytics;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class LaserPanelFragment extends ListFragment {
    private final FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

    @Nullable
    private ProfileAdapter listAdapter;
    private final LaserLayers layers = LaserLayers.getInstance();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.laser_panel, container, false);
        view.findViewById(R.id.chooseTrack).setOnClickListener(this::chooseTrack);
        view.findViewById(R.id.chooseLaser).setOnClickListener(this::chooseLaser);
        view.findViewById(R.id.addLaser).setOnClickListener(this::clickAdd);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Initialize the ListAdapter
        final LaserActivity laserActivity = (LaserActivity) getActivity();
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
        EventBus.getDefault().register(this);
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

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    void updateLayers(ProfileLayerEvent event) {
        if (listAdapter != null) {
            listAdapter.setLayers(layers.layers);
        }
    }
}
