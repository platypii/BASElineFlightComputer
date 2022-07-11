package com.platypii.baseline.views.laser;

import com.platypii.baseline.Services;
import com.platypii.baseline.databinding.LaserListBinding;
import com.platypii.baseline.events.LaserSyncEvent;
import com.platypii.baseline.lasers.LaserProfile;
import com.platypii.baseline.views.charts.layers.LaserProfileLayer;
import com.platypii.baseline.views.charts.layers.ProfileLayer;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class LaserListFragment extends Fragment implements AdapterView.OnItemClickListener {

    @NonNull
    private static String searchString = "";

    private LaserAdapter listAdapter;
    private LaserListBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = LaserListBinding.inflate(inflater, container, false);

        // Initialize the ListAdapter
        listAdapter = new LaserAdapter(getContext());
        binding.laserList.setAdapter(listAdapter);
        binding.laserList.setOnItemClickListener(this);

        // On search
        binding.laserSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String filter = binding.laserSearch.getText().toString().toLowerCase();
                listAdapter.setFilter(filter);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        // Restore search string
        binding.laserSearch.setText(searchString);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // Add profile to layers
        final LaserListItem item = listAdapter.getItem(position);
        if (item instanceof LaserListItem.ListLaser) {
            final LaserProfile laser = ((LaserListItem.ListLaser) item).laser;
            final ProfileLayer layer = new LaserProfileLayer(laser);
            Services.lasers.layers.add(layer);
            getParentFragmentManager().popBackStack();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onLaserSync(@NonNull LaserSyncEvent event) {
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
        // Save search string
        searchString = binding.laserSearch.getText().toString();
    }

}
