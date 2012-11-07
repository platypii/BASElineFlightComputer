package com.platypii.baseline.audible;

import com.platypii.baseline.data.MyDatabase;
import com.platypii.baseline.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;


// Activity to configure Audible
public class EventsListFragment extends ListFragment {

    private Context context;
    
    
    @Override
    public void onActivityCreated(Bundle savedState) {
        super.onActivityCreated(savedState);
        
        // Populate list
        setListAdapter(new EventAdapter(context, R.id.idLabel, MyDatabase.events.eventList));
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity;
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.events_list, container, false);
        
        // "Add Event" button
        View addEvent = view.findViewById(R.id.addEvent);
        addEvent.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                newEvent();
            }
        });
        
        return view;
    }
    
    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        Event event = (Event) getListAdapter().getItem(position);
        showEvent(event);
    }
    
    private void newEvent() {
        // Prompt the user for an id
        AlertDialog.Builder alert = new AlertDialog.Builder(context);
        alert.setTitle("Event Name");
        final EditText input = new EditText(context);
        alert.setView(input);
        alert.setNegativeButton("Cancel", null);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                String id = input.getText().toString();
                if(id == null || id.equals("")) {
                    Toast.makeText(context, "Invalid event name", Toast.LENGTH_SHORT);
                } else if(MyDatabase.events.events.containsKey(id)) {
                    Toast.makeText(context, "Event name already exists", Toast.LENGTH_SHORT);
                } else {
                    // Create new event
                    Event event = new Event(id);
                    showEvent(event);
                }
            }
        });
        AlertDialog dialog = alert.create();
        // Force soft keyboard
        dialog.setOnShowListener(new OnShowListener() {
            public void onShow(DialogInterface dialog) {
                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(input, InputMethodManager.SHOW_FORCED);
            }
        });
        dialog.show();
    }

    private void showEvent(Event event) {
        EventFragment frag = EventFragment.newInstance(event);
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



