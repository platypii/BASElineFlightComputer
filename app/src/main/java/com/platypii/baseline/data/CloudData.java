package com.platypii.baseline.data;

import org.json.JSONException;
import org.json.JSONObject;

/** Class representing online track info */
public class CloudData {
    public String trackUrl;
    public String trackKml;

    CloudData(String trackUrl, String trackKml) {
        this.trackUrl = trackUrl;
        this.trackKml = trackKml;
    }

    public static CloudData fromJson(String body) throws JSONException {
        final JSONObject json = new JSONObject(body);
        final String trackUrl = json.getString("trackUrl");
        final String trackKml = json.getString("trackKml");
        return new CloudData(trackUrl, trackKml);
    }

}
