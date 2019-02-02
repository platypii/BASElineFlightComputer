package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.laser.LaserProfile;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.List;

public class LaserListFragment extends ListFragment implements AdapterView.OnItemClickListener {

    private final List<LaserProfile> lasers = new ArrayList<>();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.track_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Initialize the ListAdapter
        setListAdapter(new ArrayAdapter<>(getActivity(), R.layout.track_list_item, lasers));
        getListView().setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final LaserProfile laserProfile = lasers.get(position);
        // TODO: Return to parent activity
    }

}
