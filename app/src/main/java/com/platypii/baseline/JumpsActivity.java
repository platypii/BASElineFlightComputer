package com.platypii.baseline;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.platypii.baseline.data.Jump;
import com.platypii.baseline.data.JumpLog;

import java.util.List;

public class JumpsActivity extends ListActivity {

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
    protected void onListItemClick (ListView l, View v, int position, long id) {
        final Jump jump = jumpList.get(position);
        // Open jump activity
        final Intent intent = new Intent(this, JumpActivity.class);
        intent.putExtra("JUMP_FILE", jump.logFile.getName());
        startActivity(intent);
    }
}
