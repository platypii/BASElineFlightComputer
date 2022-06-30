package com.platypii.baseline.location;

import com.platypii.baseline.places.Place;
import org.junit.Test;

import static com.platypii.baseline.location.PlaceSearch.matchPlace;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PlaceSearchTest {

    @Test
    public void matchPlaces() {
        final Place place = new Place("Fjord", "", "Norway", 68.165,16.593, 1364, "E", 1000, true);
        assertTrue(matchPlace(place, ""));
        assertTrue(matchPlace(place, "Fjord"));
        assertTrue(matchPlace(place, "Norway"));
        assertTrue(matchPlace(place, "Fjord Norway"));
        assertTrue(matchPlace(place, "fjo no"));
        assertFalse(matchPlace(place, "Sky"));
        assertTrue(matchPlace(place, "BASE"));
    }
}
