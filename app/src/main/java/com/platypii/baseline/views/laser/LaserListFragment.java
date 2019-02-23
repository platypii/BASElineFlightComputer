package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.laser.LaserLayers;
import com.platypii.baseline.laser.LaserProfile;
import com.platypii.baseline.views.charts.layers.LaserProfileLayer;
import com.platypii.baseline.views.charts.layers.ProfileLayer;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import java.util.ArrayList;
import java.util.List;

public class LaserListFragment extends ListFragment implements AdapterView.OnItemClickListener {

    private List<LaserProfile> lasers = new ArrayList<>();
    private LaserAdapter laserAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.laser_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Initialize the ListAdapter
        final List<LaserProfile> cloudLasers = Services.cloud.lasers.cache.list();
        if (cloudLasers != null) {
            lasers = cloudLasers;
        }
        laserAdapter = new LaserAdapter(getContext());
        laserAdapter.setLayers(lasers);
        setListAdapter(laserAdapter);
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final LaserListItem item = laserAdapter.getItem(position);
        if (item instanceof LaserListItem.ListLaser) {
            final LaserProfile laser = ((LaserListItem.ListLaser) item).laser;
            final ProfileLayer layer = new LaserProfileLayer(laser);
            LaserLayers.getInstance().add(layer);
            final FragmentManager fm = getFragmentManager();
            if (fm != null) fm.popBackStack();
        }
    }

}
