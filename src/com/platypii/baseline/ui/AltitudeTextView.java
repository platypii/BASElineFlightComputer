package com.platypii.baseline.ui;

import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyAltitude;
import com.platypii.baseline.data.MyAltitudeListener;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;


/**
 * A self-updating TextView to display the altitude
 * @author platypii
 *
 */
public class AltitudeTextView extends TextView {

    public AltitudeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Start altitude updates
        MyAltimeter.addListener(new MyAltitudeListener() {    
            public void doInBackground(MyAltitude alt) {}
            public void onPostExecute() {
            	AltitudeTextView.this.setText(Convert.distance(MyAltimeter.altitude));
            }
        });
        
    }

}


