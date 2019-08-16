package com.platypii.baseline.places;

import com.platypii.baseline.FileUtilCommon;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PlaceFileTest {

    private final Place kpow = new Place("Kpow", "Washington", "USA", 47.239, -123.143, 84.2, "DZ", 30000, false);

    @Test
    public void placeFile() throws IOException {
        File file = FileUtilCommon.makeFileGz("name,region,country,latitude,longitude,altitude,type,radius,wingsuitable,public\nKpow,Washington,USA,47.239,-123.143,84.2,DZ,30000,,");
        PlaceFile placeFile = new PlaceFile(file);
        assertTrue(placeFile.exists());
        assertTrue(placeFile.isFresh());

        // Parse file
        List<Place> places = placeFile.parse();
        assertEquals(1, places.size());
        Place parsed = places.get(0);
        assertEquals(kpow.name, parsed.name);
        assertEquals(kpow.region, parsed.region);
        assertEquals(kpow.country, parsed.country);
        assertEquals(kpow.lat, parsed.lat, 0.0001);
        assertEquals(kpow.lng, parsed.lng, 0.0001);
        assertEquals(kpow.alt, parsed.alt, 0.0001);
        assertEquals(kpow.objectType, parsed.objectType);

        // Delete
        placeFile.delete();
    }

}
