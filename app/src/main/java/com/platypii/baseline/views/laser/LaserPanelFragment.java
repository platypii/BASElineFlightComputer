package com.platypii.baseline.views.laser;

import com.platypii.baseline.Intents;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.events.ProfileLayerEvent;
import com.platypii.baseline.laser.LaserProfile;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.util.ABundle;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.charts.layers.LaserProfileLayer;
import com.platypii.baseline.views.charts.layers.TrackProfileLayerLocal;
import com.platypii.baseline.views.charts.layers.TrackProfileLayerRemote;
import com.platypii.baseline.views.tracks.TrackListFragment;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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
    private static final String TAG = "LaserPanel";

    private FirebaseAnalytics firebaseAnalytics;

    @Nullable
    private ProfileAdapter listAdapter;
    private View helpText;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        firebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.laser_panel, container, false);
        view.findViewById(R.id.chooseTrack).setOnClickListener(this::clickAddTrack);
        view.findViewById(R.id.chooseLaser).setOnClickListener(this::clickAddProfile);
        view.findViewById(R.id.addLaser).setOnClickListener(this::clickNewProfile);
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
            listAdapter.setLayers(Services.cloud.lasers.layers.layers);
        }
        updateViews();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onListItemClick(@NonNull ListView parent, View view, int position, long id) {
        super.onListItemClick(parent, view, position, id);
        final Object item = parent.getItemAtPosition(position);
        if (item instanceof LaserProfileLayer) {
            // Open view mode
            final LaserProfile laserProfile = ((LaserProfileLayer) item).laserProfile;
            clickLaserProfile(laserProfile);
        } else if (item instanceof TrackProfileLayerLocal) {
            // Open local track file charts
            final TrackFile track = ((TrackProfileLayerLocal) item).track;
            Log.i(TAG, "Opening local track profile " + track);
            Intents.openTrackLocal(getContext(), track);
        } else if (item instanceof TrackProfileLayerRemote) {
            // Open cloud track charts
            final CloudData track = ((TrackProfileLayerRemote) item).track;
            Log.i(TAG, "Opening cloud track profile " + track);
            Intents.openTrackRemote(getContext(), track);
        } else {
            Exceptions.report(new IllegalStateException("Unexpected list item type " + item));
        }
    }

    private void clickLaserProfile(LaserProfile laserProfile) {
        Log.i(TAG, "Opening laser profile " + laserProfile);
        firebaseAnalytics.logEvent("click_laser_profile", null);
        final Fragment frag = new LaserViewFragment();
        frag.setArguments(ABundle.of(LaserViewFragment.LASER_ID, laserProfile.laser_id));
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.laserPanel, frag)
                .addToBackStack(null)
                .commit();
    }

    private void clickAddTrack(View view) {
        firebaseAnalytics.logEvent("click_laser_track", null);
        final Fragment frag = new TrackPickerFragment();
        frag.setArguments(ABundle.of(TrackListFragment.SEARCH_KEY, "Wingsuit BASE"));
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.laserPanel, frag)
                .addToBackStack(null)
                .commit();
    }

    private void clickAddProfile(View view) {
        firebaseAnalytics.logEvent("click_laser_list", null);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.laserPanel, new LaserListFragment())
                .addToBackStack(null)
                .commit();
    }

    private void clickNewProfile(View view) {
        firebaseAnalytics.logEvent("click_laser_add", null);
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.laserPanel, new LaserEditFragment())
                .addToBackStack(null)
                .commit();
    }

    private void updateViews() {
        if (Services.cloud.lasers.layers.layers.isEmpty()) {
            helpText.setVisibility(View.VISIBLE);
        } else {
            helpText.setVisibility(View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateLayers(ProfileLayerEvent event) {
        if (listAdapter != null) {
            listAdapter.setLayers(Services.cloud.lasers.layers.layers);
        }
        updateViews();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
