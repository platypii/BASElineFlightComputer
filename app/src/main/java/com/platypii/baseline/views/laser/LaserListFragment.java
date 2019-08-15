package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.events.LaserSyncEvent;
import com.platypii.baseline.laser.LaserProfile;
import com.platypii.baseline.views.charts.layers.LaserProfileLayer;
import com.platypii.baseline.views.charts.layers.ProfileLayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class LaserListFragment extends Fragment implements AdapterView.OnItemClickListener {

    private LaserAdapter listAdapter;
    private ListView listView;
    private EditText searchBox;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.laser_list, container, false);
        listView = view.findViewById(R.id.laser_list);
        searchBox = view.findViewById(R.id.laser_search);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Initialize the ListAdapter
        listAdapter = new LaserAdapter(getContext());
        listAdapter.populateItems();
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);

        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String filter = searchBox.getText().toString().toLowerCase();
                listAdapter.setFilter(filter);
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        final LaserListItem item = listAdapter.getItem(position);
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
        listAdapter.populateItems();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

}
