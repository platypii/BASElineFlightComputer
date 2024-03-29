package com.platypii.baseline.views.laser;

import com.platypii.baseline.Intents;
import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.databinding.LaserPanelBinding;
import com.platypii.baseline.events.ProfileLayerEvent;
import com.platypii.baseline.lasers.LaserProfile;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.tracks.TrackMetadata;
import com.platypii.baseline.util.ABundle;
import com.platypii.baseline.util.Analytics;
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
import android.widget.AdapterView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class LaserPanelFragment extends Fragment implements AdapterView.OnItemClickListener {
    private static final String TAG = "LaserPanel";

    @Nullable
    private ProfileAdapter listAdapter;
    private LaserPanelBinding binding;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = LaserPanelBinding.inflate(inflater, container, false);

        // On clicks
        binding.chooseTrack.setOnClickListener(this::clickAddTrack);
        binding.chooseLaser.setOnClickListener(this::clickAddProfile);
        binding.addLaser.setOnClickListener(this::clickNewProfile);

        // Initialize the ListAdapter
        final Activity laserActivity = getActivity();
        if (laserActivity != null) {
            listAdapter = new ProfileAdapter(laserActivity); // TODO: Don't pass activity to adapter
            binding.profilesList.setAdapter(listAdapter);
            binding.profilesList.setOnItemClickListener(this);
        }

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (listAdapter != null) {
            listAdapter.setLayers(Services.lasers.layers.layers);
        }
        updateViews();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
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
            final TrackMetadata track = ((TrackProfileLayerRemote) item).track;
            Log.i(TAG, "Opening cloud track profile " + track);
            Intents.openTrackRemote(getContext(), track);
        } else {
            Exceptions.report(new IllegalStateException("Unexpected list item type " + item));
        }
    }

    /**
     * Open laser profile view
     */
    private void clickLaserProfile(@NonNull LaserProfile laserProfile) {
        Analytics.logEvent(getContext(), "click_laser_profile", ABundle.of("laser_id", laserProfile.laser_id));
        final Fragment frag = new LaserViewFragment();
        frag.setArguments(ABundle.of(LaserViewFragment.LASER_ID, laserProfile.laser_id));
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.laserPanel, frag)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Open track picker
     */
    private void clickAddTrack(View view) {
        Analytics.logEvent(getContext(), "click_laser_add_track", null);
        final Fragment frag = new TrackPickerFragment();
        frag.setArguments(ABundle.of(TrackListFragment.SEARCH_KEY, "Wingsuit BASE"));
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.laserPanel, frag)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Open laser profile picker
     */
    private void clickAddProfile(View view) {
        Analytics.logEvent(getContext(), "click_laser_add_profile", null);
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.laserPanel, new LaserListFragment())
                .addToBackStack(null)
                .commit();
    }

    /**
     * Open new laser profile panel
     */
    private void clickNewProfile(View view) {
        Analytics.logEvent(getContext(), "click_laser_new_profile", null);
        getParentFragmentManager()
                .beginTransaction()
                .replace(R.id.laserPanel, new LaserEditFragment())
                .addToBackStack(null)
                .commit();
    }

    private void updateViews() {
        if (binding.helpProfiles != null) {
            boolean isEmpty = Services.lasers.layers.layers.isEmpty();
            binding.helpProfiles.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void updateLayers(ProfileLayerEvent event) {
        if (listAdapter != null) {
            listAdapter.setLayers(Services.lasers.layers.layers);
        }
        updateViews();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
