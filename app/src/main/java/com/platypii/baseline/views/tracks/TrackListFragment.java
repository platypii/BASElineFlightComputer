package com.platypii.baseline.views.tracks;

import com.platypii.baseline.R;
import com.platypii.baseline.events.SyncEvent;

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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class TrackListFragment extends Fragment implements AdapterView.OnItemClickListener {

    public static final String SEARCH_KEY = "search_string";
    protected TrackAdapter listAdapter;
    private ListView listView;
    @Nullable
    private EditText searchBox;
    @Nullable
    private View tracksEmptyLabel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.track_list, container, false);
        listView = view.findViewById(R.id.track_list);
        searchBox = view.findViewById(R.id.track_search);
        tracksEmptyLabel = view.findViewById(R.id.tracks_empty);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Initialize the ListAdapter
        listAdapter = new TrackAdapter(getActivity());
        listView.setAdapter(listAdapter);
        listView.setOnItemClickListener(this);

        // Load filter string from arguments
        final Bundle args = getArguments();
        if (args != null) {
            final String filter = args.getString(SEARCH_KEY, "");
            if (!filter.isEmpty()) {
                if (searchBox != null) searchBox.setText(filter);
                listAdapter.setFilter(filter);
            }
        }

        if (searchBox != null) {
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
    }

    @Override
    public void onResume() {
        super.onResume();

        // Update the views
        updateList();

        // Listen for sync updates
        EventBus.getDefault().register(this);
    }

    private void updateList() {
        // Update list from track cache
        listAdapter.notifyDataSetChanged();

        // Handle no-tracks case
        final boolean isEmpty = listAdapter.isEmpty();
        if (tracksEmptyLabel != null) {
            tracksEmptyLabel.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        }
        if (searchBox != null) {
            searchBox.setEnabled(!isEmpty);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        listAdapter.clickItem(position, getActivity());
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        searchBox = null;
        tracksEmptyLabel = null;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncEvent(@NonNull SyncEvent event) {
        updateList();
    }

}
