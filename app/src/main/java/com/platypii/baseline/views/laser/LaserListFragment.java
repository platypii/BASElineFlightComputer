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
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.List;

public class LaserListFragment extends ListFragment implements AdapterView.OnItemClickListener {

    private List<LaserProfile> lasers = new ArrayList<>();

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
        setListAdapter(new ArrayAdapter<>(getContext(), R.layout.track_list_item, R.id.list_item_name, lasers));
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final ProfileLayer layer = new LaserProfileLayer(lasers.get(position));
        LaserLayers.getInstance().add(layer);
        final FragmentManager fm = getFragmentManager();
        if (fm != null) fm.popBackStack();
    }

}
