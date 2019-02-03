package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.views.tracks.TrackListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.firebase.analytics.FirebaseAnalytics;

public class LaserPanelFragment extends ListFragment {
    private final FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());

    private ProfileAdapter listAdapter;

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
        listAdapter = new ProfileAdapter(laserActivity, laserActivity.layers);
        setListAdapter(listAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateLayers();
    }

    private void chooseTrack(View view) {
        firebaseAnalytics.logEvent("click_laser_track", null);
        startActivityForResult(new Intent(getActivity(), TrackListActivity.class), LaserActivity.TRACK_REQUEST);
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

    public void updateLayers() {
        listAdapter.notifyDataSetChanged();
    }
}
