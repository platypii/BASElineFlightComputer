package com.platypii.baseline.tracks;

import com.platypii.baseline.measurements.MLocation;
import com.platypii.baseline.util.Numbers;
import android.util.Log;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

/**
 * Parse location data from track file
 */
public class TrackFileData {
    private static final String TAG = "TrackFileData";

    public static List<MLocation> getTrackData(File trackFile) {
        final List<MLocation> data = new ArrayList<>();
        // Read file line by line
        // TODO minsdk19: InputStreamReader(,StandardCharsets.UTF_8)
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(trackFile))))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(",gps,")) {
                    // Parse the line
                    final String[] split = line.split(",");
                    final long millis = Numbers.parseLong(split[0], -1);
                    final double lat = Numbers.parseDouble(split[4]);
                    final double lon = Numbers.parseDouble(split[5]);
                    final double alt_gps = Numbers.parseDouble(split[6]);
                    final double climb = Double.NaN;
                    final double vN = Numbers.parseDouble(split[7]);
                    final double vE = Numbers.parseDouble(split[8]);
                    final float hAcc = Float.NaN;
                    final float pdop = Float.NaN;
                    final float hdop = Float.NaN;
                    final float vdop = Float.NaN;
                    final int satUsed = 0;
                    final int satInView = 0;
                    final MLocation loc = new MLocation(millis, lat, lon, alt_gps, climb, vN, vE, hAcc, pdop, hdop, vdop, satUsed, satInView);
                    data.add(loc);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error getting track data from " + trackFile, e);
        }
        return data;
    }
}
