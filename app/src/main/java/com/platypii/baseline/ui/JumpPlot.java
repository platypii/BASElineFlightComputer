package com.platypii.baseline.ui;

import com.platypii.baseline.data.Jump;
import com.platypii.baseline.data.Measurement;

import android.content.Context;
import android.graphics.Canvas;
import android.text.format.DateFormat;
import android.util.AttributeSet;


public class JumpPlot extends PlotView {

	// Jump data
	private Jump jump;

	
    public JumpPlot(Context context, AttributeSet attrs) {
        super(context, attrs);
        
        float density = getResources().getDisplayMetrics().density;
        padding_top = (int) (12 * density);
        padding_bottom = (int) (2 * density);
        padding_left = (int) (6 * density);
        padding_right = (int) (6 * density);
        
        min.left = 0;
        min.right = 60000; // 1min
        max.bottom = 0;
        min.top = 20 * Convert.FT;
        
        x_major_units = 60000;
        y_major_units = 1 * Convert.FT;
    }

    /**
     * Load a jump into the plot
     * @param jump The jump to load
     */
    public void loadJump(Jump jump) {
    	this.jump = jump;
    	// Load jump data from database
    	jump.loadJump();
        // Redraw
        postInvalidate();
    }

    @Override
    public void drawData(Canvas canvas) {
    	// TODO: Update live data before drawing
    	if(jump != null && jump.loaded) {
    		synchronized(jump.jumpData) {
		        for(Measurement measurement : jump.jumpData) {
		            double x = measurement.timeMillis;
		            double y = measurement.altitude;
		            int color = 0xff0000ee; // default
		            if(measurement.flightMode == null || measurement.flightMode.equals("") || measurement.flightMode.equals("Ground"))
		            	color = 0xff00bb00;
		            else if(measurement.flightMode.equals("Climb"))
		            	color = 0xffbb0000;
		            else if(measurement.flightMode.equals("Freefall"))
		            	color = 0xff0000dd;
		            else if(measurement.flightMode.equals("Flight"))
		            	color = 0xff0077cc;
		            drawPoint(canvas, x, y, 2, color);
		            // series.addPoint(x, y);
	        	}
    		}
    	}
    }
    
    @Override
    public String formatX(double x) {
        if(Math.abs(x) < EPSILON || jump == null)
            return "";
        else {
        	if(jump.dataEnd - jump.dataStart < 86400000) // Less than 1 day
                return DateFormat.format("kk:mm:ss", (long) x).toString();
        	else
                return DateFormat.format("MM/dd/yyyy", (long) x).toString();
        }
    }
    @Override
    public String formatY(double y) {
        return Convert.distance(y, 0);
    }

}

