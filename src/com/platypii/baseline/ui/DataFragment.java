package com.platypii.baseline.ui;

import com.platypii.baseline.data.MyAltimeter;
import com.platypii.baseline.data.MyLocation;
import com.platypii.baseline.data.MyLocationListener;
import com.platypii.baseline.data.MyLocationManager;
import com.platypii.baseline.R;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class DataFragment extends Fragment {

    private TextView speedLabel;
    private TextView glideLabel;
    private TextView vxLabel;
    private TextView vyLabel;
    
    // Periodic UI updates
    private Handler handler = new Handler();
    private static final int updateInterval = 80; // in milliseconds


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        final View view = inflater.inflate(R.layout.data, container, false);

        // Find UI elements:
        speedLabel = (TextView)view.findViewById(R.id.speedLabel);
        glideLabel = (TextView)view.findViewById(R.id.glideLabel);
        vxLabel = (TextView)view.findViewById(R.id.vxLabel);
        vyLabel = (TextView)view.findViewById(R.id.vyLabel);
        
        // Initial update
        updateGPS(MyLocationManager.lastLoc);
        
        // GPS updates
        MyLocationManager.addListener(new MyLocationListener() {
            public void onLocationChanged(final MyLocation loc) {
                handler.post(new Runnable() {
                    public void run() {
                        updateGPS(loc);
                    }
                });
            }
        });
        
        // Periodic UI updates
        handler.post(new Runnable() {
            public void run() {
                AltimeterFragment.updateAlti(view);
                vyLabel.setText(Convert.speed(MyAltimeter.climb));
                handler.postDelayed(this, updateInterval);
            }
        });
        
        return view;
    }

    /**
     * Updates the UI based on new location data
     * @param loc The new location data
     */
    private void updateGPS(MyLocation loc) {
        if(loc != null) {
            speedLabel.setText(Convert.speed(MyLocationManager.speed));
            glideLabel.setText(Convert.glide(MyLocationManager.glide));
            vxLabel.setText(Convert.speed(MyLocationManager.groundSpeed));
            vyLabel.setText(Convert.speed(MyAltimeter.climb));
        } else {
            speedLabel.setText("");
            glideLabel.setText("");
            vxLabel.setText("");
            vyLabel.setText("");
        }
    }
    
}


