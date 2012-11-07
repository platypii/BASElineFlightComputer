package com.platypii.baseline.audible;

import com.platypii.baseline.R;

import android.content.Context;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView.OnEditorActionListener;


/**
 * Class to represent a sensor and range event as an interactive view
 */
public class SensorEventPanel {

	// private final View panel;
    private final SensorEvent sensorEvent;

    // SensorEvent
    public MySensor sensor;
    public double min;
    public double max;

    // Panel
    private Spinner sensorSpinner;
    private TextView minLabel;
    private TextView maxLabel;
    private DynamicSeekBar minBar;
    private DynamicSeekBar maxBar;
    private View rangePanel; // The range panel
    

    /**
     * Loads a SensorEvent into the panel
     */
    public SensorEventPanel(Context context, View panel, SensorEvent sensorEvent) {
    	// this.panel = panel;
    	this.sensorEvent = sensorEvent;
    	
        this.sensor = sensorEvent.sensor;
        this.min = sensorEvent.min;
        this.max = sensorEvent.max;

        sensorSpinner = (Spinner)panel.findViewById(R.id.sensorSpinner);
        minLabel = (TextView)panel.findViewById(R.id.minLabel);
        maxLabel = (TextView)panel.findViewById(R.id.maxLabel);
        minBar = (DynamicSeekBar)panel.findViewById(R.id.minBar);
        maxBar = (DynamicSeekBar)panel.findViewById(R.id.maxBar);
        rangePanel = panel.findViewById(R.id.sensorRange);

        // Set up the panel
        sensorSpinner.setAdapter(new ArrayAdapter<MySensor>(context, R.layout.spinner_item, MySensors.getSensors()));
        sensorSpinner.setSelection(indexOf(MySensors.getSensors(), sensorEvent.sensor));

        // Update on user interaction
        sensorSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MySensor selected = (MySensor) sensorSpinner.getSelectedItem();
                if(!sensor.equals(selected)) {
                    // New sensor selected
                    sensor = selected;
                    if(sensor.getName().equals("None")) {
                    	// Hide range panel
                    	rangePanel.setVisibility(View.GONE);
                    } else {
                    	// Show range panel and initialize
                    	rangePanel.setVisibility(View.VISIBLE);
                        minBar.setValue(0);
                        maxBar.setValue(0);
                        min = sensor.getDefaultMin();
                        max = sensor.getDefaultMax();
                        minBar.setMinMax(min, max, sensor.getDefaultStep());
                        maxBar.setMinMax(min, max, sensor.getDefaultStep());
                        minBar.setValue(min);
                        maxBar.setValue(max);
                    }
                    updateSensorEvent();
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        if(sensorEvent.sensor == null) {
        	android.util.Log.e("EventFragment", "WTF null");
        } else if(sensorEvent.sensor.getName().equals("None")) {
        	rangePanel.setVisibility(View.GONE);
        } else {
        	rangePanel.setVisibility(View.VISIBLE);
            // Set range
            minBar.setMinMax(sensorEvent.sensor.getDefaultMin(), sensorEvent.sensor.getDefaultMax(), sensorEvent.sensor.getDefaultStep());
            maxBar.setMinMax(sensorEvent.sensor.getDefaultMin(), sensorEvent.sensor.getDefaultMax(), sensorEvent.sensor.getDefaultStep());
            // Set sensor event bounds
            minBar.setValue(sensorEvent.min);
            maxBar.setValue(sensorEvent.max);
        }
        
        minBar.setOnSeekBarChangeListener(changeListener);
        maxBar.setOnSeekBarChangeListener(changeListener);
        minLabel.setOnEditorActionListener(editListener);
        maxLabel.setOnEditorActionListener(editListener);

        updateSensorEvent();

    }
    
    /**
     * Updates labels, range, etc based on the sensor event panel
     */
    private void updateSensorEvent() {
        
        // Update values
        MySensor sensor = (MySensor) sensorSpinner.getSelectedItem();
        if(sensor.getName().equals("None")) {
        	rangePanel.setVisibility(View.GONE);
            minLabel.setText(null);
            maxLabel.setText(null);
        } else {
            minLabel.setText(sensor.formatValue(min));
            maxLabel.setText(sensor.formatValue(max));
        }

    }
    
    /**
     * Saves the sensor event from the panel into the SensorEvent
     */
    public void saveSensorEvent() {
        sensorEvent.sensor = (MySensor) sensorSpinner.getSelectedItem();
        sensorEvent.min = min;
        sensorEvent.max = max;
    }
    

    /**
     * Listeners to update the UI on user input
     */
    private OnSeekBarChangeListener changeListener = new OnSeekBarChangeListener() {
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        	min = minBar.getValue();
            max = maxBar.getValue();
            updateSensorEvent();
        }
        public void onStartTrackingTouch(SeekBar seekBar) {}
        public void onStopTrackingTouch(SeekBar seekBar) {}
    };
    private OnEditorActionListener editListener = new OnEditorActionListener() {
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			try {
				min = sensor.parseValue(minLabel.getText().toString());
				max = sensor.parseValue(maxLabel.getText().toString());
			} catch(NumberFormatException e) {}
            updateSensorEvent();
			return false;
		}
    };

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
