package com.platypii.baseline.tracks;

import com.platypii.baseline.places.Place;

public class MockTrackMetadata extends TrackMetadata {
    public MockTrackMetadata() {
        super(
                "1234",
                System.currentTimeMillis(),
                "2018ish",
                "https://baseline.ws/tracks/1234/track.csv",
                "https://baseline.ws/tracks/1234/track.kml",
                new Place("TestExit", "", "Norway", 59.033, 6.586, Double.NaN, "E", 2000, true),
                null
        );
    }
}
