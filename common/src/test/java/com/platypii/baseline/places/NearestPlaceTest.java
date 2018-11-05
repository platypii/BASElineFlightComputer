package com.platypii.baseline.places;

import com.platypii.baseline.measurements.MLocation;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class NearestPlaceTest {

    private final MLocation seattle = new MLocation(1541348400990L, 47.60, -122.33, 100.0, -2.0, 3.0, 4.0, 0f, 0f, 0f, 0f, 0, 0);

    @Test
    public void nearestPlace() {
        List<Place> placeList = new ArrayList<>();
        placeList.add(new Place("Kpow", "", "USA", 47.239, -123.143, 84.2, "DZ", 30000));
        placeList.add(new Place("Moab", "", "USA", 38.57, -109.55, 1300, "DZ", 30000));
        Places places = new Places() {
            @Override
            List<Place> getPlaces() {
                return placeList;
            }
        };
        NearestPlace nearestPlace = new NearestPlace(places);

        assertEquals("Kpow", nearestPlace.cached(seattle).name);
        assertEquals("USA", nearestPlace.cached(seattle).country);
    }

}
