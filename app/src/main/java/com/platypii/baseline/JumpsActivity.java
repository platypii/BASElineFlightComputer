package com.platypii.baseline;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.platypii.baseline.data.CloudData;
import com.platypii.baseline.data.Jump;
import com.platypii.baseline.data.JumpLog;
import com.platypii.baseline.data.TrackAdapter;

import java.util.List;

public class JumpsActivity extends ListActivity implements AdapterView.OnItemLongClickListener {

    private List<Jump> jumpList;
    private ArrayAdapter<Jump> listAdapter;

    // Periodic UI updates
    private final Handler handler = new Handler();
    private final int updateInterval = 100; // milliseconds

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

        // Start periodic UI updates
        handler.post(new Runnable() {
            public void run() {
                update();
                handler.postDelayed(this, updateInterval);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Update the ListAdapter
        jumpList.clear();
        jumpList.addAll(JumpLog.getJumps(getApplicationContext()));
        listAdapter.notifyDataSetChanged();
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

    private void update() {
        // Refresh sync status
//        listAdapter.notifyDataSetChanged();
    }

}
