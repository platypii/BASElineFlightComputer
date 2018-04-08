package com.platypii.baseline.tracks;

import android.support.annotation.NonNull;
import android.util.Log;
import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;

/**
 * Tools to strip down track data for faster processing
 */
public class TrackAbbrv {
    private static final String TAG = "TrackAbbrv";

    public static void abbreviate(@NonNull File trackFile, @NonNull File abbrvFile) {
        final long startTime = System.currentTimeMillis();
        // Read file line by line
        // TODO minsdk19: InputStreamReader(,StandardCharsets.UTF_8)
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(trackFile))))) {
            try (FileWriter writer = new FileWriter(abbrvFile)) {
                boolean firstLine = true;
                String line;

                while ((line = br.readLine()) != null) {
                    if (firstLine) {
                        firstLine = false;
                        writer.write(line);
                        writer.write('\n');
                    } else if (!line.contains(",alt,") && !line.contains(",grv,") && !line.contains(",rot,") && !line.contains(",acc,")) {
                        // Write gps only
                        writer.write(line);
                        writer.write('\n');
                    }
                }
            }
        } catch (EOFException e) {
            // Still error but less verbose
            Log.e(TAG, "Premature end of gzip track file " + trackFile + " to " + abbrvFile + "\n" + e);
        } catch (IOException e) {
            Log.e(TAG, "Error abbreviated track data from " + trackFile + " to " + abbrvFile, e);
        }
        Log.i(TAG, "Abbreviated track " + (trackFile.length()>>10) + "kb -> " + (abbrvFile.length()>>10) + "kb in " + (System.currentTimeMillis() - startTime) + "ms");
    }

}
