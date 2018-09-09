package com.platypii.baseline.places;

import com.platypii.baseline.util.IOUtil;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Manages the place database
 */
public class FetchPlaces {
    private static final String TAG = "FetchPlaces";

    private static final String placesUrl = "https://baseline.ws/places.csv";

    /**
     * Fetch places from BASEline server and saves it as a file
     */
    static void get(File placeFile) throws IOException {
        Log.i(TAG, "Downloading places");
        final URL url = new URL(placesUrl);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        try {
            // Read response
            final int status = conn.getResponseCode();
            if(status == 200) {
                // Make places directory
                placeFile.getParentFile().mkdir();
                // Read body to place file
                final OutputStream os = new FileOutputStream(placeFile);
                IOUtil.copy(conn.getInputStream(), os);
                Log.i(TAG, "Place file download successful");
            } else {
                throw new IOException("Places.get http status code " + status);
            }
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Fetch places hash from BASEline server
     */
    static String head() throws IOException {
        Log.i(TAG, "Checking for new places");
        final URL url = new URL(placesUrl);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("HEAD");
        try {
            // Read response
            final int status = conn.getResponseCode();
            if(status == 200) {
                final String etag = conn.getHeaderField("eTag");
                Log.i(TAG, "Got latest places hash " + etag);
                return etag;
            } else {
                throw new IOException("Places.head http status code " + status);
            }
        } finally {
            conn.disconnect();
        }
    }

}
