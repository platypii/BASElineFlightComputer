package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.laser.LaserProfile;
import com.platypii.baseline.views.tracks.TrackListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.google.firebase.analytics.FirebaseAnalytics;

public class LaserPanelFragment extends Fragment {
    private final FirebaseAnalytics firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());;

    private TextView laserName;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.laser_panel, container, false);
        view.findViewById(R.id.chooseTrack).setOnClickListener(this::chooseTrack);
        view.findViewById(R.id.chooseLaser).setOnClickListener(this::chooseLaser);
        view.findViewById(R.id.clickLaserAdd).setOnClickListener(this::clickAdd);
        laserName = view.findViewById(R.id.laserName);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        final LaserProfile laser = ((LaserActivity) getActivity()).laserProfile;
        if (laser != null) {
            laserName.setText(laser.name);
        }
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

}
