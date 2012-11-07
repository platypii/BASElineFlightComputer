package com.platypii.baseline.audible;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;


// A seek bar with floating point min and max, that you can extend by dragging to the edges. 
public class DynamicSeekBar extends SeekBar {
    
    private double step = 1;
    public double min = 0;
    public double max = 1;
    private static final int STEPS = 800;
    
    private double value = 0;
    

    public DynamicSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        super.setMax(STEPS);
        super.setOnSeekBarChangeListener(changeListener);
    }

    public void setMinMax(double min, double max, double step) {
        this.min = min;
        this.max = max;
        this.step = step;
        setValue(value);
    }
    
    public void setValue(double value) {
        this.value = value;
        
        // If value is outside of bounds, expand
        if(value < min) {
            min = value;
        }
        if(max < value) {
            max = value;
        }
        
        // Transform value
        // int progress = (int) (0.1 * STEPS + (value - min) * (0.8 * STEPS) / (max - min));
        int progress = (int) ((value - min) * STEPS / (max - min));
        this.setProgress(progress);
    }

    public double getValue() {
        return value;
    }

    // Change listeners
    // Updates the UI on changes
    private OnSeekBarChangeListener changeListener = new OnSeekBarChangeListener() {
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser) {
                // Drag to shift range
                /*
                if(progress < STEPS * 0.1) {
                    // Expand minimum bound
                    min -= step * (STEPS * 0.1 - progress);
                    max -= step * (STEPS * 0.1 - progress);
                    progress = (int) (STEPS * 0.11); // reset progress
                    setProgress(progress);
                    Log.w("DynamicSeekBar", "Extending minimum");
                } else if(progress > STEPS * 0.9) {
                    // Expand maximum bound
                    min += step * (progress - STEPS * 0.9);
                    max += step * (progress - STEPS * 0.9);
                    progress = (int) (STEPS * 0.89); // reset progress
                    setProgress(progress);
                    Log.w("DynamicSeekBar", "Extending maximum");
                }
                */
                
                // Transform progress into value
                // value = min + (max - min) * (progress - .1 * STEPS) / (0.8 * STEPS);
                value = min + (max - min) * progress / STEPS;
                value = Math.round(value / step) * step; // round to nearest step
            }
            
            // Notify secondary listener
            if(changeListener2 != null) {
                changeListener2.onProgressChanged(seekBar, progress, fromUser);
            }
        }
        public void onStartTrackingTouch(SeekBar seekBar) {
            if(changeListener2 != null)
                changeListener2.onStartTrackingTouch(seekBar);
        }
        public void onStopTrackingTouch(SeekBar seekBar) {
            if(changeListener2 != null)
                changeListener2.onStopTrackingTouch(seekBar);
        }
    };
    
    // Allow for secondary changeListener
    private OnSeekBarChangeListener changeListener2 = null;
    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener listener2) {
        this.changeListener2 = listener2;
    }
    
}
