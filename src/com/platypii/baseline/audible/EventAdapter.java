package com.platypii.baseline.audible;

import com.platypii.baseline.data.MyDatabase;
import com.platypii.baseline.R;

import java.util.List;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;


public class EventAdapter extends ArrayAdapter<Event> {

    private LayoutInflater inflater;
    private List<Event> events;
    

    public EventAdapter(Context context, int textViewResourceId, List<Event> events) {
        super(context, textViewResourceId, events);
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.events = events;
    }
    
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = inflater.inflate(R.layout.checkbox_list_item, null);
        }
        final Event event = events.get(position);
        if(event != null) {
            // The "checkbox"
            View indicator = convertView.findViewById(R.id.indicator);
            final CheckBox enabledBox = (CheckBox) indicator.findViewById(R.id.enabledBox);
            // Clicking outside the "checkbox" should also change the state
            indicator.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    enabledBox.toggle();
                    event.enabled = !event.enabled;
                    if(!event.enabled)
                        event.terminate();
                    
                    // Save to database
                    MyDatabase.events.put(event);
                }
            });
            enabledBox.setChecked(event.enabled);
            
            // ID Label
            TextView idLabel = (TextView) convertView.findViewById(R.id.idLabel);
            idLabel.setText(event.id);
        }
        return convertView;
    }
    

}