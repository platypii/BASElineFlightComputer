package com.platypii.baseline.ui;

import com.platypii.baseline.data.Jump;
import com.platypii.baseline.data.MyDatabase;
import com.platypii.baseline.R;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


// Activity to configure Audible
public class JumpsListFragment extends ListFragment {

    private Context context;

    
    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
        
        // Populate list
        setListAdapter(new ArrayAdapter<Jump>(context, R.layout.simple_list_item, MyDatabase.jumps.jumps));
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.jumps_list, container, false);
    }
    
    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        Jump jump = (Jump) getListAdapter().getItem(position);
        showJump(jump);
    }

    private void showJump(Jump jump) {
        JumpFragment frag = JumpFragment.newInstance(jump);
        // Fragment swap
        FragmentTransaction ft = this.getFragmentManager().beginTransaction();
        ft.replace(R.id.details, frag);
        ft.commit();
    }

    public void updateList() {
        ArrayAdapter<?> listAdapter = (ArrayAdapter<?>) getListAdapter();
        listAdapter.notifyDataSetChanged();
    }


    
}



