package com.platypii.baseline.views.map;

import com.platypii.baseline.R;
import androidx.annotation.NonNull;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class PlaceIcons {

    private static final BitmapDescriptor b = BitmapDescriptorFactory.fromResource(R.drawable.shadow_b);
    private static final BitmapDescriptor a = BitmapDescriptorFactory.fromResource(R.drawable.shadow_a);
    private static final BitmapDescriptor s = BitmapDescriptorFactory.fromResource(R.drawable.shadow_s);
    private static final BitmapDescriptor e = BitmapDescriptorFactory.fromResource(R.drawable.shadow_e);
    private static final BitmapDescriptor dz = BitmapDescriptorFactory.fromResource(R.drawable.shadow_dz);
    private static final BitmapDescriptor other = BitmapDescriptorFactory.fromResource(R.drawable.ic_pin);

    @NonNull
    public static BitmapDescriptor icon(String objectType) {
        switch (objectType) {
            case "B": return PlaceIcons.b;
            case "A": return PlaceIcons.a;
            case "S": return PlaceIcons.s;
            case "E": return PlaceIcons.e;
            case "DZ": return PlaceIcons.dz;
            default: return PlaceIcons.other;
        }
    }

}
