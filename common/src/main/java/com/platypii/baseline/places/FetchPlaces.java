package com.platypii.baseline.places;

import com.platypii.baseline.cloud.AuthException;
import com.platypii.baseline.cloud.AuthState;
import com.platypii.baseline.util.IOUtil;

import android.util.Log;
import androidx.annotation.NonNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Downloads the place database.
 */
class FetchPlaces {
    private static final String TAG = "FetchPlaces";

    private static final String placesUrl = "https://baseline.ws/places.csv";

    /**
     * Fetch places from BASEline server and saves it as a file
     */
    static void get(@NonNull File placeFile) throws IOException, AuthException {
        Log.i(TAG, "Downloading places");
        final URL url = new URL(placesUrl);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Accept-Encoding", "gzip");
        final String token = AuthState.getToken();
        if (token != null) {
            conn.setRequestProperty("Cookie", token);
        }
//        conn.setRequestProperty("User-Agent", "BASEline Android App/" + BuildConfig.VERSION_NAME); // Doesn't work in common lib
        try {
            // Read response
            final int status = conn.getResponseCode();
            if (status == 200) {
                // Make places directory
                placeFile.getParentFile().mkdir();
                // Read body to place file
                final OutputStream os = new FileOutputStream(placeFile);
                IOUtil.copy(conn.getInputStream(), os);
                Log.i(TAG, "Places downloaded to file " + placeFile + " (" + (placeFile.length() >> 10) + " KiB)");
            } else if (status == 304) {
                Log.e(TAG, "Places file not modified");
            } else {
                throw new IOException("Places.get http status code " + status);
            }
        } finally {
            conn.disconnect();
        }
    }

}
