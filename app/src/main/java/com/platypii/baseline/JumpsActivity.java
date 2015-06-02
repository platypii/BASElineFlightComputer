package com.platypii.baseline;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.platypii.baseline.data.CloudData;
import com.platypii.baseline.data.Jump;
import com.platypii.baseline.data.JumpLog;
import com.platypii.baseline.data.SyncStatus;
import com.platypii.baseline.data.TrackAdapter;

import java.util.List;

public class JumpsActivity extends ListActivity implements AdapterView.OnItemLongClickListener, SyncStatus.SyncListener {

    private List<Jump> jumpList;
    private ArrayAdapter<Jump> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jumps);

        // Initialize the ListAdapter
        jumpList = JumpLog.getJumps(getApplicationContext());
        listAdapter = new TrackAdapter(this, R.layout.jump_list_item, jumpList);
        setListAdapter(listAdapter);

        final ListView listView = getListView();
        listView.setOnItemLongClickListener(this);

        // Listen for sync updates
        SyncStatus.addListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Update the ListAdapter
        jumpList.clear();
        jumpList.addAll(JumpLog.getJumps(getApplicationContext()));
        listAdapter.notifyDataSetChanged();

        // Listen for sync updates
        SyncStatus.addListener(this);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        final Jump jump = jumpList.get(position);
        Intents.openJumpActivity(this, jump);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View v, int position, long id) {
        final Jump jump = jumpList.get(position);
        final CloudData cloudData = jump.getCloudData();
        if(cloudData != null) {
            // Open KML directly
            Intents.openTrackKml(this, cloudData);
        } else {
            // Open track view
            Intents.openJumpActivity(this, jump);
        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Listen for sync updates
        SyncStatus.removeListener(this);
    }
    @Override
    public void syncUpdate() {
        // Refresh sync status
        listAdapter.notifyDataSetChanged();
    }
}
