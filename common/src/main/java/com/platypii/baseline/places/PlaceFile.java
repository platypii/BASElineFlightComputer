package com.platypii.baseline.places;

import com.platypii.baseline.util.CSVHeader;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

import static com.platypii.baseline.util.CSVParse.getColumnDouble;
import static com.platypii.baseline.util.CSVParse.getColumnString;
import static com.platypii.baseline.util.CSVParse.getColumnYes;

/**
 * Loads places from gzipped CSV
 */
class PlaceFile {
    private static final String TAG = "ParsePlaces";

    private static final String placeFilename = "places/places.csv.gz";
    private static final long ttl = 24 * 60 * 60 * 1000; // Update if data is older than 1 day

    @NonNull
    final File file;

    PlaceFile(@NonNull Context context) {
        this(new File(context.getFilesDir(), placeFilename));
    }
    PlaceFile(@NonNull File file) {
        this.file = file;
    }

    boolean exists() {
        return file.exists();
    }

    boolean isFresh() {
        return file.exists() && System.currentTimeMillis() < file.lastModified() + ttl;
    }

    /**
     * Parse places from local file into list of Places
     */
    @NonNull
    List<Place> parse() throws IOException {
        Log.i(TAG, "Loading places from file (" + (file.length()>>10) + " KiB)");
        final List<Place> places = new ArrayList<>();
        // Read place file csv (gzipped)
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file)), StandardCharsets.UTF_8))) {
            // Parse header column
            String line = br.readLine();
            final CSVHeader columns = new CSVHeader(line);
            // Parse data rows
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty()) {
                    try {
                        final String[] row = line.split(",");
                        final String name = getColumnString(row, columns, "name");
                        final String region = getColumnString(row, columns, "region");
                        final String country = getColumnString(row, columns, "country");
                        final double latitude = getColumnDouble(row, columns, "latitude");
                        final double longitude = getColumnDouble(row, columns, "longitude");
                        final double altitude = getColumnDouble(row, columns, "altitude");
                        final String objectType = getColumnString(row, columns, "type");
                        final double radius = getColumnDouble(row, columns, "radius");
                        final boolean wingsuitable = getColumnYes(row, columns, "wingsuitable");
                        places.add(new Place(name, region, country, latitude, longitude, altitude, objectType, radius, wingsuitable));
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing place file", e);
                    }
                }
            }
        }
        Log.i(TAG, "Loaded " + places.size() + " places");
        return places;
    }

    void delete() {
        // TODO: Possible race deleting file while parsing
        if (file.exists() && !file.delete()) {
            Log.w(TAG, "Failed to delete place file");
        }
    }

}
