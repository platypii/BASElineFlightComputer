package com.platypii.baseline.location;

import com.platypii.baseline.altimeter.MyAltimeter;
import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.util.Numbers;
import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

// TODO: Switch to GnssStatus when minsdk >= 24
@SuppressWarnings("deprecation")
class LocationProviderAndroid extends LocationProvider implements LocationListener, GpsStatus.Listener {
    private static final String TAG = "LocationProviderAndroid";

    private final MyAltimeter alti;

    // Android Location manager
    private LocationManager manager;

    // Satellite data comes from GpsStatusListener
    private int satellitesInView = -1;
    private int satellitesUsed = -1;

    @NonNull
    @Override
    protected String providerName() {
        return TAG;
    }

    LocationProviderAndroid(MyAltimeter alti) {
        this.alti = alti;
    }

    /**
     * Start location updates
     * @param context The Application context
     */
    @Override
    public void start(@NonNull Context context) throws SecurityException {
        manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if(manager.getProvider(LocationManager.GPS_PROVIDER) != null) {
            try {
                // TODO: Specify looper thread?
                manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
                manager.addGpsStatusListener(this);
            } catch (Exception e) {
                Exceptions.report(e);
            }
        }
    }

    /** Android location listener */
    @Override
    public void onLocationChanged(@NonNull Location loc) {
        // TODO: minsdk26: loc.getVerticalAccuracyMeters();
        // TODO: minsdk26: loc.getSpeedAccuracyMetersPerSecond()
        if (Numbers.isReal(loc.getLatitude()) && Numbers.isReal(loc.getLongitude())) {
            final float hAcc;
            if (loc.hasAccuracy())
                hAcc = loc.getAccuracy();
            else
                hAcc = Float.NaN;

            final long lastFixMillis = loc.getTime();
            final double latitude = loc.getLatitude();
            final double longitude = loc.getLongitude();
            final double altitude_gps;
            if (loc.hasAltitude()) {
                altitude_gps = loc.getAltitude();
            } else {
                altitude_gps = Double.NaN;
            }
            final double groundSpeed;
            if (loc.hasSpeed()) {
                groundSpeed = loc.getSpeed();
            } else {
                groundSpeed = Double.NaN;
            }
            final double bearing;
            if (loc.hasBearing()) {
                bearing = loc.getBearing();
            } else {
                bearing = Double.NaN;
            }

            final double vN;
            final double vE;
            if(Numbers.isReal(groundSpeed) && Numbers.isReal(bearing)) {
                vN = groundSpeed * Math.cos(Math.toRadians(bearing));
                vE = groundSpeed * Math.sin(Math.toRadians(bearing));
            } else {
                vN = vE = Double.NaN;
            }

            final float pdop, hdop, vdop;
            pdop = hdop = vdop = Float.NaN;

            // Update official location
            updateLocation(new MLocation(
                    lastFixMillis, latitude, longitude, altitude_gps, alti.climb, vN, vE,
                    hAcc, pdop, hdop, vdop, satellitesUsed, satellitesInView));
        }
    }
    @Override
    public void onProviderDisabled(String provider) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    private GpsStatus gpsStatus;
    @Override
    public void onGpsStatusChanged(int event) {
        switch (event) {
            case GpsStatus.GPS_EVENT_STARTED:
                Log.i(TAG, "GPS started");
                break;
            case GpsStatus.GPS_EVENT_STOPPED:
                Log.i(TAG, "GPS stopped");
                break;
            case GpsStatus.GPS_EVENT_FIRST_FIX:
                Log.i(TAG, "GPS first fix");
                break;
            case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
                if(manager != null) {
                    try {
                        gpsStatus = manager.getGpsStatus(gpsStatus);
                        final Iterable<GpsSatellite> satellites = gpsStatus.getSatellites();
                        int count = 0;
                        int used = 0;
                        for (GpsSatellite sat : satellites) {
                            count++;
                            if (sat.usedInFix()) {
                                used++;
                            }
                        }
                        // if(satellitesInView != count || satellitesUsed != used) {
                        //     Log.v(TAG, "Satellite Status: " + satellitesUsed + "/" + satellitesInView);
                        // }
                        satellitesInView = count;
                        satellitesUsed = used;
                    } catch(SecurityException e) {
                        Exceptions.report(e);
                    }
                }
        }
    }

    @Override
    public void stop() {
        super.stop();
        if(manager != null) {
            manager.removeGpsStatusListener(this);
            try {
                manager.removeUpdates(this);
            } catch(SecurityException e) {
                Log.w(TAG, "Exception while stopping android location updates", e);
            }
            manager = null;
        }
    }
}
