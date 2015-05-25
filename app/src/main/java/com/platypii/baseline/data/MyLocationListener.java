package com.platypii.baseline.data;

import com.platypii.baseline.data.measurements.MLocation;

// Used by MyLocationManager to notify activities of updated location
public interface MyLocationListener {
    
    void onLocationChanged(MLocation loc);

}
