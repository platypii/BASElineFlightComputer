package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.events.LaserSyncEvent;
import com.platypii.baseline.laser.LaserProfile;
import com.platypii.baseline.views.charts.layers.LaserProfileLayer;
import com.platypii.baseline.views.charts.layers.ProfileLayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.ListFragment;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class LaserListFragment extends ListFragment implements AdapterView.OnItemClickListener {

    private LaserAdapter laserAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.laser_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Initialize the ListAdapter
        laserAdapter = new LaserAdapter(getContext());
        laserAdapter.populateItems();
        setListAdapter(laserAdapter);
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final LaserListItem item = laserAdapter.getItem(position);
        if (item instanceof LaserListItem.ListLaser) {
            final LaserProfile laser = ((LaserListItem.ListLaser) item).laser;
            final ProfileLayer layer = new LaserProfileLayer(laser);
            Services.cloud.lasers.layers.add(layer);
            final FragmentManager fm = getFragmentManager();
            if (fm != null) fm.popBackStack();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLaserSync(@NonNull LaserSyncEvent event) {
        laserAdapter.populateItems();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

}
