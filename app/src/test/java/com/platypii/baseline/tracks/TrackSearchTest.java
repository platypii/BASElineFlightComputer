package com.platypii.baseline.tracks;

import com.platypii.baseline.places.Place;

import org.junit.Test;

import static com.platypii.baseline.tracks.TrackSearch.matchTrack;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TrackSearchTest {

    @Test
    public void matchTracks() {
        final TrackMetadata track = new TrackMetadata("tid", 10000000L, "nowish", "http", "kml", null, null, "Corvid", "OSP");
        assertTrue(matchTrack(track, ""));
        assertTrue(matchTrack(track, " "));
        assertFalse(matchTrack(track, "BASE"));
        assertFalse(matchTrack(track, "Skydive"));
        assertTrue(matchTrack(track, "Corvi"));
        assertTrue(matchTrack(track, "Corvid"));
        assertTrue(matchTrack(track, "OSP"));
        assertTrue(matchTrack(track, "co sp"));
        assertFalse(matchTrack(track, "crv"));
        assertTrue(matchTrack(track, "Córvïd"));
    }

    @Test
    public void matchTracksWithPlace() {
        final TrackMetadata trackNoPlace = new TrackMetadata("tid", 10000000L, "nowish", "http", "kml", null, null, null, null);
        assertFalse(matchTrack(trackNoPlace, "Norway"));
        final Place place = new Place("Fjord", "", "Norway", 68.165,16.593, 1364, "E", 1000, true);
        final TrackMetadata track = new TrackMetadata("tid", 10000000L, "nowish", "http", "kml", place, null, null, null);
        assertTrue(matchTrack(track, "Fjord"));
        assertTrue(matchTrack(track, "Norway"));
        assertTrue(matchTrack(track, "Fjord Norway"));
        assertTrue(matchTrack(track, "fjo no"));
    }

    @Test
    public void matchTrackJumpType() {
        final Place place = new Place("Fjord", "", "Norway", 68.165,16.593, 1364, "E", 1000, true);
        final TrackMetadata track = new TrackMetadata("tid", 10000000L, "nowish", "http", "kml", place, "Skydive", null, null);
        assertTrue(matchTrack(track, "Sky"));
        assertFalse(matchTrack(track, "BASE"));
    }
}
