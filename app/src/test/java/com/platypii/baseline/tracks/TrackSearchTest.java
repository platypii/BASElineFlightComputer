package com.platypii.baseline.tracks;

import com.platypii.baseline.places.Place;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TrackSearchTest {

    @Test
    public void matchTrack() {
        final TrackMetadata track = new TrackMetadata("tid", 10000000L, "nowish", "http", "kml", null);
        track.suit = "Corvid";
        track.canopy = "OSP";
        assertTrue(TrackSearch.matchTrack(track, ""));
        assertTrue(TrackSearch.matchTrack(track, " "));
        assertFalse(TrackSearch.matchTrack(track, "BASE"));
        assertFalse(TrackSearch.matchTrack(track, "Skydive"));
        assertTrue(TrackSearch.matchTrack(track, "Corvi"));
        assertTrue(TrackSearch.matchTrack(track, "Corvid"));
        assertTrue(TrackSearch.matchTrack(track, "OSP"));
        assertTrue(TrackSearch.matchTrack(track, "co sp"));
        assertFalse(TrackSearch.matchTrack(track, "crv"));
    }

    @Test
    public void matchTrackWithPlace() {
        final TrackMetadata trackNoPlace = new TrackMetadata("tid", 10000000L, "nowish", "http", "kml", null);
        assertFalse(TrackSearch.matchTrack(trackNoPlace, "Norway"));
        final Place place = new Place("Fjord", "", "Norway", 68.165,16.593, 1364, "E", 1000, true);
        final TrackMetadata track = new TrackMetadata("tid", 10000000L, "nowish", "http", "kml", place);
        assertTrue(TrackSearch.matchTrack(track, "Fjord"));
        assertTrue(TrackSearch.matchTrack(track, "Norway"));
        assertTrue(TrackSearch.matchTrack(track, "Fjord Norway"));
        assertTrue(TrackSearch.matchTrack(track, "fjo no"));
    }
}
