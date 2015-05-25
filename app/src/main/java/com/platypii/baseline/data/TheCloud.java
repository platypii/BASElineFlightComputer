package com.platypii.baseline.data;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TheCloud {

    private static final String platypiiIndustries = "http://dev.platypiiindustries.com"; // TODO
    private static final String postUrl = platypiiIndustries + "/baseline/tracks";

    public static String upload(Jump jump) {
        if(isNetworkConnected()) {
            // Upload to platypii industries
            try {
                final String path = postJump(jump);
                return platypiiIndustries + path;
            } catch(IOException e) {
                Log.e("Cloud", "Failed to upload file", e);
                return null;
            }
        } else {
            Log.w("Cloud", "Network not connected");
            return null;
        }
    }

    private static String postJump(Jump jump) throws IOException {
        final File file = jump.logFile;
        final URL url = new URL(postUrl);
        final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        final long contentLength = file.length();
        try {
            conn.setDoOutput(true);
            // Write to OutputStream
            if(contentLength > Integer.MAX_VALUE) {
                conn.setChunkedStreamingMode(0);
            } else {
                conn.setFixedLengthStreamingMode((int) contentLength);
            }
            final OutputStream os = new BufferedOutputStream(conn.getOutputStream());
            copy(new FileInputStream(file), os);
            // Read response
            final int status = conn.getResponseCode();
            if(status == 200) {
                // Read body
                return toString(conn.getInputStream());
            } else {
                throw new IOException("http status code " + status);
            }
        } finally {
            conn.disconnect();
        }
    }

    private static void copy(InputStream input, OutputStream output) throws IOException {
        final byte buffer[] = new byte[1024];
        int bytesRead = 0;
        while((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        output.flush();
    }

    private static String toString(InputStream input) throws IOException {
        final StringBuilder builder = new StringBuilder();
        byte buffer[] = new byte[1024];
        int bytesRead = 0;
        while((bytesRead = input.read(buffer)) != -1) {
            builder.append(new String(buffer, 0, bytesRead));
        }
        return builder.toString();
    }

    private static boolean isNetworkConnected() {
//        final ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
//        final NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
//        return (networkInfo != null && networkInfo.isConnected());
        return true; // TODO
    }

}
