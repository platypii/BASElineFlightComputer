package com.platypii.baseline.views.charts;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.platypii.baseline.events.ChartFocusEvent;
import com.platypii.baseline.views.tracks.TrackDataActivity;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class FlightProfileFragment extends Fragment {

    @Nullable
    private FlightProfile flightProfile;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        flightProfile = new FlightProfileTouchable(getContext(), null);
        // Get track data from parent activity
        final Activity parent = getActivity();
        if (parent instanceof TrackDataActivity) {
            ((TrackDataActivity) parent).trackData.thenAccept(trackData -> {
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        flightProfile = null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onChartFocus(ChartFocusEvent event) {
        if (flightProfile != null) {
            flightProfile.onFocus(event.location);
        }
    }

}
