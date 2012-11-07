package com.platypii.baseline.data;


// Used by MyLocationManager to notify activities of updated location
public interface MyLocationListener {
    
    public void onLocationChanged(MyLocation loc);

}
