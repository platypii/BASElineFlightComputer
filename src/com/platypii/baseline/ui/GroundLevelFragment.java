package com.platypii.baseline.ui;

import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyAltitude;
import com.platypii.baseline.data.MyAltitudeListener;
import com.platypii.baseline.ui.GroundLevelWidget.GroundLevelWidgetListener;
import com.platypii.baseline.R;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


// Sets ground level
public class GroundLevelFragment extends Fragment {
    
    // Views
    private GroundLevelWidget widget;
    private TextView altitudeLabel;
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.groundlevel, container, false);

        // Find views
        altitudeLabel = (TextView)view.findViewById(R.id.altitudeLabel);
        widget = (GroundLevelWidget)view.findViewById(R.id.groundLevelWidget);

        // Notify us when the user changes the altitude offset, or the pressure changes
        widget.addListener(new GroundLevelWidgetListener() {
            public void onGroundLevelChanged() {
                update();
            }
        });
        MyAltimeter.addListener(new MyAltitudeListener() {
            public void doInBackground(MyAltitude alt) {}
            public void onPostExecute() {
                update();
            }
        });

    	return view;
    }

    private void update() {
        // Update altitude and pressure
        double altitude = MyAltimeter.altitude + widget.offset;
        if(Double.isNaN(altitude)) altitude = widget.offset; // TODO: Pre-set the offset?
        altitudeLabel.setText(Convert.distance(altitude));
        widget.invalidate(); // redraw
    }
}





