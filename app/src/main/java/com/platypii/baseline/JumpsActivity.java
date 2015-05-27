package com.platypii.baseline;

import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.platypii.baseline.data.Jump;
import com.platypii.baseline.data.JumpLog;
import com.platypii.baseline.data.TheCloud;

import java.util.List;

public class JumpsActivity extends ListActivity implements AdapterView.OnItemLongClickListener {

    private List<Jump> jumpList;
    private ArrayAdapter<Jump> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_jumps);

        // Initialize the ListAdapter
        jumpList = JumpLog.getJumps(getApplicationContext());
        listAdapter = new ArrayAdapter<>(this, R.layout.jump_list, R.id.listText, jumpList);
        setListAdapter(listAdapter);

        final ListView listView = getListView();
        listView.setOnItemLongClickListener(this);
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
        final String cloudUrl = jump.getCloudUrl();
        if(cloudUrl != null) {
            // Open cloud url in browser
            Log.i("Jumps", "Track synced, opening " + cloudUrl);
            final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(cloudUrl));
            startActivity(browserIntent);
        } else {
            Log.i("Jumps", "Track not synced");
            Toast.makeText(this, "Track not synced, uploading...", Toast.LENGTH_LONG).show();
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
}
