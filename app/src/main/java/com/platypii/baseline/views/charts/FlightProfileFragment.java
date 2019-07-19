package com.platypii.baseline.views.charts;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.platypii.baseline.events.ChartFocusEvent;
import com.platypii.baseline.tracks.TrackData;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class FlightProfileFragment extends Fragment {

    private FlightProfile flightProfile;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        flightProfile = new FlightProfileTouchable(getContext(), null);
        // Get track data from parent fragment
        final Fragment parent = getParentFragment();
        if (parent instanceof ChartsFragment) {
            ((ChartsFragment) parent).trackData.thenAccept((TrackData trackData) -> {
                flightProfile.loadTrack(trackData);
                flightProfile.postInvalidate();
            });
        }
        return flightProfile;
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
    public void onChartFocus(ChartFocusEvent event) {
        flightProfile.onFocus(event.location);
    }

}
