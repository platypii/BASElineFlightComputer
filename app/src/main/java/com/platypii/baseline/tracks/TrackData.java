package com.platypii.baseline.tracks;

import com.google.firebase.crash.FirebaseCrash;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Class representing online track info
 */
public class TrackData {
    public final String track_id;
    public final long date;
    public final String date_string;
    public final String track_url;
    public final String track_kml;
    public final String location;

    private TrackData(String track_id, long date, String date_string, String track_url, String track_kml, String location) {
        this.track_id = track_id;
        this.date = date;
        this.date_string = date_string;
        this.track_url = track_url;
        this.track_kml = track_kml;
        this.location = location;
    }

    public static TrackData fromJson(JSONObject json) throws JSONException {
        final String track_id = json.getString("track_id");
        final long date = json.getLong("date");
        final String date_string = json.optString("date_string");
        final String track_url = json.optString("track_url");
        final String track_kml = json.optString("track_kml");
        final String location = json.optString("location");
        return new TrackData(track_id, date, date_string, track_url, track_kml, location);
    }

    public JSONObject toJson() {
        final JSONObject obj = new JSONObject();
        try {
            obj.put("track_id", track_id);
            obj.put("date", date);
            obj.put("date_string", date_string);
            obj.put("track_url", track_url);
            obj.put("track_kml", track_kml);
            obj.put("location", location);
            return obj;
        } catch (JSONException e) {
            FirebaseCrash.report(e);
            return null;
        }
    }

}
