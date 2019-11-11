package com.platypii.baseline.places;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Convert;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NearestPlaceTest {

    private final MLocation shelton = new MLocation(1541348400990L, 47.22, -123.11, 100.0, -2.0, 3.0, 4.0, 0f, 0f, 0f, 0f, 0, 0);
    private final Place kpow = new Place("Kpow", "", "USA", 47.239, -123.143, 84.2, "DZ", 30000, false);
    private final Place moab = new Place("Moab", "", "USA", 38.57, -109.55, 1300, "DZ", 30000, false);

    @Test
    public void nearestPlace() {
        NearestPlace nearestPlace = new NearestPlace(makePlaces());
        assertEquals("Kpow", nearestPlace.cached(shelton).name);
        assertEquals("USA", nearestPlace.cached(shelton).country);
    }

    @Test
    public void getString() {
        NearestPlace nearestPlace = new NearestPlace(makePlaces());
        Convert.metric = false;
        assertEquals("Kpow (2 mi)", nearestPlace.getString(shelton));
    }

    @NonNull
    private Places makePlaces() {
        List<Place> placeList = new ArrayList<>();
        placeList.add(kpow);
        placeList.add(moab);
        return new Places() {
            @Override
            List<Place> getPlaces() {
                return placeList;
            }
        };
    }

}
