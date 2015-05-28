package com.platypii.baseline;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.platypii.baseline.data.CloudData;
import com.platypii.baseline.data.Jump;
import com.platypii.baseline.data.JumpLog;
import com.platypii.baseline.data.TheCloud;
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
        // Check if synced or not
        final CloudData cloudData = jump.getCloudData();
        if(cloudData != null) {
            // Open cloud url in browser
            final String url = cloudData.trackUrl;
            Log.i("Jumps", "Track already synced, opening " + url);
            final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        } else {
            Log.i("Jumps", "Track not synced, uploading...");
            Toast.makeText(this, "Syncing track...", Toast.LENGTH_LONG).show();
            // Try to start sync
            TheCloud.uploadAsync(jump);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        // Open jump activity
        final Jump jump = jumpList.get(i);
        final Intent intent = new Intent(this, JumpActivity.class);
        intent.putExtra("JUMP_FILE", jump.logFile.getName());
        startActivity(intent);
        return false;
    }

    private void update() {
        // Refresh sync status
        listAdapter.notifyDataSetChanged();
    }

}
