package com.platypii.baseline;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import com.platypii.baseline.data.Jump;
import com.platypii.baseline.data.JumpLog;
import com.platypii.baseline.data.TrackAdapter;
import com.platypii.baseline.events.SyncEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.util.ArrayList;
import java.util.List;

public class JumpsActivity extends ListActivity {

    private List<Jump> jumpList;
    private TrackAdapter listAdapter;

    private View tracksEmptyLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jumps);

        tracksEmptyLabel = findViewById(R.id.tracks_empty);

        // Initialize the ListAdapter
        jumpList = new ArrayList<>();
        listAdapter = new TrackAdapter(this, jumpList);
        setListAdapter(listAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Update the ListAdapter
        jumpList.clear();
        jumpList.addAll(JumpLog.getJumps(getApplicationContext()));
        listAdapter.notifyDataSetChanged();

        // Handle no-tracks case
        if(jumpList.size() > 0) {
            tracksEmptyLabel.setVisibility(View.GONE);
        } else {
            tracksEmptyLabel.setVisibility(View.VISIBLE);
        }

        // Listen for sync updates
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final Jump track = listAdapter.getTrack(position);
        if(track != null) {
            Intents.openJumpActivity(this, track);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onSyncEvent(SyncEvent event) {
        // Update sync status in the list
        listAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStart() {
        super.onStart();
        // Start flight services
        Services.start(this);
    }
    @Override
    public void onStop() {
        super.onStop();
        // Stop flight services
        Services.stop();
    }
}
