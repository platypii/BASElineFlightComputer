package com.platypii.baseline.places;

import com.platypii.baseline.util.IOUtil;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Downloads the place database.
 * Uses if-modified-since header and 304 to save bandwidth.
 */
class FetchPlaces {
    private static final String TAG = "FetchPlaces";

    private static final String placesUrl = "https://baseline.ws/places.csv";

    /**
     * Fetch places from BASEline server and saves it as a file
     */
    static void get(@NonNull File placeFile) throws IOException {
        Log.i(TAG, "Downloading places");
        final URL url = new URL(placesUrl);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Accept-Encoding", "gzip");
//        conn.setRequestProperty("User-Agent", "BASEline Android App/" + BuildConfig.VERSION_NAME); // Doesn't work in common lib
        // Send If-Modified-Since header to help with caching
        if (placeFile.exists()) {
            final String dateString = httpDateString(placeFile.lastModified());
            conn.setRequestProperty("If-Modified-Since", dateString);
        }
        try {
            // Read response
            final int status = conn.getResponseCode();
            if (status == 200) {
                // Make places directory
                placeFile.getParentFile().mkdir();
                // Read body to place file
                final OutputStream os = new FileOutputStream(placeFile);
                IOUtil.copy(conn.getInputStream(), os);
                Log.i(TAG, "Places downloaded to file " + placeFile + " (" + (placeFile.length()>>10) + " KiB)");
            } else if (status == 304) {
                Log.e(TAG, "Places file not modified");
            } else {
                throw new IOException("Places.get http status code " + status);
            }
        } finally {
            conn.disconnect();
        }
    }

    /**
     * Formats a Date as an http compatible date string (rfc 2616)
     * @param millis milliseconds since the epoch
     */
    private static String httpDateString(long millis) {
        final Date date = new Date(millis);
        final SimpleDateFormat httpDateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        return httpDateFormat.format(date);
    }

}
