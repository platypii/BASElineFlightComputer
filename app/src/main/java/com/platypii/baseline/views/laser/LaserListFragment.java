package com.platypii.baseline.views.laser;

import com.platypii.baseline.R;
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
        listAdapter = new LaserAdapter(getContext(), searchString);
        binding.laserList.setAdapter(listAdapter);
        binding.laserList.setOnItemClickListener(this);
        binding.laserSearchClear.setOnClickListener(searchClearListener);
        // Restore search string
        binding.laserSearch.setText(searchString);
        // On search
        binding.laserSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchString = binding.laserSearch.getText().toString();
                listAdapter.setFilter(searchString);
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSearchClear();
            }
        });

        return binding.getRoot();
    }

    @NonNull
    private final View.OnClickListener searchClearListener = view -> {
        binding.laserSearch.setText("");
    };

    private void updateSearchClear() {
        if (binding.laserSearch.getText().toString().isEmpty()) {
            binding.laserSearchClear.setImageResource(R.drawable.search);
            binding.laserSearchClear.setClickable(false);
        } else {
            binding.laserSearchClear.setImageResource(R.drawable.search_clear);
            binding.laserSearchClear.setClickable(true);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
        updateSearchClear();
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
    }

}
