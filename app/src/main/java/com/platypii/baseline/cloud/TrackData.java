package com.platypii.baseline.cloud;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class representing online track info
 */
class TrackData {
    final String track_id;
    final long date;
    final String date_string;
    final String location;

    private TrackData(String track_id, long date, String date_string, String location) {
        this.track_id = track_id;
        this.date = date;
        this.date_string = date_string;
        this.location = location;
    }

    static TrackData fromJson(JSONObject json) throws JSONException {
        final String track_id = json.getString("track_id");
        final long date = json.getLong("date");
        final String date_string = json.getString("date_string");
        final String location = json.getString("location");

        return new TrackData(track_id, date, date_string, location);
    }

}
