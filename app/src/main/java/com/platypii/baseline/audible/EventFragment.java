package com.platypii.baseline.audible;

import com.platypii.baseline.data.MyDatabase;
import com.platypii.baseline.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import android.widget.Spinner;
import android.widget.TextView;


// TODO: Show current value of sensor


public class EventFragment extends Fragment {

    private Context context;
    public Event event;
    
    // Views
    private TextView idLabel;
    private Spinner triggerModeSpinner;
    private View triggerSensorView;
    private Spinner soundSpinner;
    private CheckBox loopBox;
    private DynamicSeekBar balanceBar;
    private View modifierSensorView;
    private CheckBox fadeBox;

    // These handle the mapping from screen ui to internal SensorEventRepresentation
    private SensorEventPanel triggerSensorPanel;
    private SensorEventPanel modifierSensorPanel;

    private Button saveButton;
    private Button deleteButton;
    
    
    /**
     * Create a new instance
     */
    public static EventFragment newInstance(Event event) {
        EventFragment frag = new EventFragment();
        // Supply event input as an argument
        Bundle args = new Bundle();
        args.putParcelable("event", event);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.context = activity;
    }
    
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        bundle = this.getArguments();
        event = bundle.getParcelable("event");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.event, container, false);

        // Find views
        idLabel = (TextView) view.findViewById(R.id.idLabel);
        triggerModeSpinner = (Spinner) view.findViewById(R.id.triggerMode);
        triggerSensorView = view.findViewById(R.id.triggerPanel);
        soundSpinner = (Spinner) view.findViewById(R.id.soundSpinner);
        loopBox = (CheckBox) view.findViewById(R.id.loopBox);
        balanceBar = (DynamicSeekBar) view.findViewById(R.id.balanceBar);
        modifierSensorView = view.findViewById(R.id.modifierPanel);
        fadeBox = (CheckBox) view.findViewById(R.id.fadeBox);
        saveButton = (Button) view.findViewById(R.id.eventSave);
        deleteButton = (Button) view.findViewById(R.id.eventDelete);
        
        // Load event
        idLabel.setText(event.id);
        triggerModeSpinner.setAdapter(new ArrayAdapter<String>(context, R.layout.spinner_item, Event.modes));
        triggerModeSpinner.setSelection(indexOf(Event.modes, event.triggerModeEvent.mode));
        triggerSensorPanel = new SensorEventPanel(context, triggerSensorView, event.triggerSensorEvent);
        soundSpinner.setAdapter(new ArrayAdapter<String>(context, R.layout.spinner_item, MySoundManager.samples));
        soundSpinner.setSelection(MySoundManager.samples.indexOf(event.sampleName));
        loopBox.setChecked(event.sampleLoop);
        balanceBar.setMinMax(0, 1, 0.01);
        balanceBar.setValue(event.modifierBalance);
        modifierSensorPanel = new SensorEventPanel(context, modifierSensorView, event.modifierSensorEvent);
        fadeBox.setChecked(event.modifierFade);
        
        // Called when the user clicks save
        saveButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Save UI to event
                saveEvent();
                Toast.makeText(context, "Saved event " + event.id, Toast.LENGTH_LONG).show();
            }
        });
        
        // Called when the user clicks delete
        deleteButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // Confirm with user
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle("Really delete event, " + event.id + "?");
                alert.setNegativeButton("Cancel", null);
                alert.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Clear fragment from view
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.remove(EventFragment.this);
                        ft.commit();
                        
                        // Delete event
                        MyDatabase.events.remove(event);
                        event.terminate();
                        
                        // Notify events list
                        FragmentManager fm = getFragmentManager();
                        EventsListFragment eventsListFrag = (EventsListFragment) fm.findFragmentById(R.id.eventsListFrag);
                        eventsListFrag.updateList();
                    }
                });
                alert.show();
            }
        });
        
        return view;
    }

    /**
     * Save the UI settings to the Event
     */
    public void saveEvent() {
        // Terminate event if it's firing
        event.terminate();

        // Save the new event parameters
        event.triggerModeEvent = new ModeEvent(triggerModeSpinner.getSelectedItem().toString());
        triggerSensorPanel.saveSensorEvent();
        event.sampleName = (String) soundSpinner.getSelectedItem();
        event.sampleLoop = loopBox.isChecked();
        event.modifierBalance = (float) balanceBar.getValue();
        modifierSensorPanel.saveSensorEvent();
        event.modifierFade = fadeBox.isChecked();
        
        if(!MyDatabase.events.events.containsKey(event.id)) {
            // Notify events list
            FragmentManager fm = getFragmentManager();
            EventsListFragment eventsListFrag = (EventsListFragment) fm.findFragmentById(R.id.eventsListFrag);
            eventsListFrag.updateList();
        }
        
        // Save event to database
        MyDatabase.events.put(event);
        
    }
    
    /**
     * Helper to find the index of an object in an array
     */
    private static int indexOf(Object arr[],Object obj) {
        for(int i = 0; i < arr.length; i++)
            if(arr[i].equals(obj))
                return i;
        return 0;
    }

}



