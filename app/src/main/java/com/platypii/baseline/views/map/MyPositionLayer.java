package com.platypii.baseline.views.map;

import com.platypii.baseline.R;
import com.platypii.baseline.Services;
import com.platypii.baseline.util.Numbers;

import androidx.annotation.NonNull;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

class MyPositionLayer implements MapLayer {

    private final Marker myPositionMarker;
    @NonNull
    private final BitmapDescriptor myposition1;
    @NonNull
    private final BitmapDescriptor myposition2;

    MyPositionLayer(@NonNull GoogleMap map) {
        myposition1 = BitmapDescriptorFactory.fromResource(R.drawable.myposition1);
        myposition2 = BitmapDescriptorFactory.fromResource(R.drawable.myposition2);

        myPositionMarker = map.addMarker(new MarkerOptions()
                .position(new LatLng(0, 0))
                .visible(false)
                .icon(myposition2)
                .anchor(0.5f, 0.5f)
                .flat(true)
        );
    }

    @Override
    public void update() {
        if (Services.location.isFresh()) {
            myPositionMarker.setVisible(true);
            myPositionMarker.setPosition(Services.location.lastLoc.latLng());
            final double groundSpeed = Services.location.groundSpeed();
            final double bearing = Services.location.bearing();
            if (Numbers.isReal(bearing) && groundSpeed > 0.1) {
                // Speed > 0.2mph
                myPositionMarker.setIcon(myposition1);
                myPositionMarker.setRotation((float) bearing);
            } else {
                myPositionMarker.setIcon(myposition2);
            }
        }
    }

}
