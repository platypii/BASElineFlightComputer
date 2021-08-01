package com.platypii.baseline.views.tracks;

import com.platypii.baseline.Services;
import com.platypii.baseline.databinding.TrackListBinding;
import com.platypii.baseline.events.SyncEvent;

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

public class TrackListFragment extends Fragment implements AdapterView.OnItemClickListener {

    public static final String SEARCH_KEY = "search_string";
    protected TrackAdapter listAdapter;
    private TrackListBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = TrackListBinding.inflate(inflater, container, false);

        // Initialize the ListAdapter
        listAdapter = new TrackAdapter(getActivity());
        binding.trackList.setAdapter(listAdapter);
        binding.trackList.setOnItemClickListener(this);

        // Load filter string from arguments
        final Bundle args = getArguments();
        if (args != null) {
            final String filter = args.getString(SEARCH_KEY, "");
            if (!filter.isEmpty()) {
                binding.trackSearch.setText(filter);
                listAdapter.setFilter(filter);
            }
        }

        // On search
        binding.trackSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                final String filter = binding.trackSearch.getText().toString().toLowerCase();
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

        // Listen for sync updates
        EventBus.getDefault().register(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        // Update the views
        updateList();
        // Update tracks async, since users of this activity probably care about fresh data
        Services.tracks.listAsync(getContext(), false);
    }

    private void updateList() {
        // Update list from track cache
        listAdapter.notifyDataSetChanged();

        // Handle no-tracks case
        final boolean isEmpty = listAdapter.isEmpty();
        binding.tracksEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        binding.trackSearch.setEnabled(!isEmpty);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        listAdapter.clickItem(position, getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncEvent(@NonNull SyncEvent event) {
        updateList();
    }

}
