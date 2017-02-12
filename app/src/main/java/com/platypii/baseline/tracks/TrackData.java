package com.platypii.baseline.tracks;

import com.google.firebase.crash.FirebaseCrash;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class representing online track info
 */
public class TrackData {
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

    public static TrackData fromJson(JSONObject json) throws JSONException {
        final String track_id = json.getString("track_id");
        final long date = json.getLong("date");
        final String date_string = json.getString("date_string");
        final String location = json.optString("location");
        return new TrackData(track_id, date, date_string, location);
    }

    public JSONObject toJson() {
        final JSONObject obj = new JSONObject();
        try {
            obj.put("track_id", track_id);
            obj.put("date", date);
            obj.put("date_string", date_string);
            obj.put("location", location);
            return obj;
        } catch (JSONException e) {
            FirebaseCrash.report(e);
            return null;
        }
    }

}
