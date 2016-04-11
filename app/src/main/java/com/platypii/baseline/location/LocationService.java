package com.platypii.baseline.location;

import android.content.Context;
import android.support.annotation.NonNull;

import com.platypii.baseline.data.measurements.MLocation;

/**
 * Meta location provider that uses bluetooth, nmea, or android location source
 */
public class LocationService extends LocationProvider {

    // hAcc comes from android location provider
    private float hAcc = Float.NaN;

    private final LocationProviderNMEA locationProviderNMEA = new LocationProviderNMEA();
    private final LocationProviderAndroid locationProviderAndroid = new LocationProviderAndroid();
    private final MyLocationListener nmeaListener = new MyLocationListener() {
        @Override
        public void onLocationChanged(MLocation loc) {
            if(Float.isNaN(loc.hAcc)) {
                loc.hAcc = hAcc;
            }
            updateLocation(loc);
        }
        @Override
        public void onLocationChangedPostExecute() {}
    };
    private final MyLocationListener androidListener = new MyLocationListener() {
        @Override
        public void onLocationChanged(MLocation loc) {
            // Only use android location if we aren't getting NMEA
            if(!locationProviderNMEA.nmeaReceived) {
                updateLocation(loc);
            }
            hAcc = loc.hAcc;
        }
        @Override
        public void onLocationChangedPostExecute() {}
    };

    @Override
    public void start(@NonNull Context context) {
        locationProviderNMEA.start(context);
        locationProviderAndroid.start(context);
        locationProviderNMEA.addListener(nmeaListener);
        locationProviderAndroid.addListener(androidListener);
    }

    @Override
    public void stop() {
        super.stop();
        locationProviderNMEA.stop();
        locationProviderAndroid.stop();
    }

}
