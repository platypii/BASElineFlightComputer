package com.platypii.baseline.cloud;

import org.json.JSONException;
import org.json.JSONObject;

/** Class representing online track info */
public class CloudData {
    public final String trackUrl;
    public final String trackKml;

    CloudData(String trackUrl, String trackKml) {
        this.trackUrl = trackUrl;
        this.trackKml = trackKml;
    }

    static CloudData fromJson(JSONObject json) throws JSONException {
        final String trackUrl = json.getString("trackUrl");
        final String trackKml = json.getString("trackKml");
        return new CloudData(trackUrl, trackKml);
    }

}
