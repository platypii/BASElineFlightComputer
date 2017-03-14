package com.platypii.baseline;

import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.tracks.TrackFile;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;
import com.google.firebase.crash.FirebaseCrash;

public class Intents {
    private static final String TAG = "Intents";

    /* Request codes used to invoke user interactions */
    static final int RC_SIGN_IN = 0;
    static final int RC_LOCATION = 1;
    public static final int RC_TTS_DATA = 2;

    /** Open jump activity */
    public static void openTrackActivity(@NonNull Context context, TrackFile trackFile) {
        final Intent intent = new Intent(context, TrackActivity.class);
        intent.putExtra(TrackActivity.EXTRA_TRACK_FILE, trackFile.file.getName());
        context.startActivity(intent);
    }
    public static void openTrackDataActivity(@NonNull Context context, CloudData track) {
        final Intent intent = new Intent(context, TrackDataActivity.class);
        intent.putExtra(TrackDataActivity.EXTRA_TRACK_ID, track.track_id);
        context.startActivity(intent);
    }

    /** Open track url in browser */
    static void openTrackUrl(@NonNull Context context, String url) {
        if(url != null && !url.isEmpty()) {
            // Add mobile flag
            if(!url.contains("?")) {
                url += "?mobile";
            }
            Log.i(TAG, "Opening track url " + url);
            final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(intent);
        } else {
            Log.e(TAG, "Cannot open missing track url");
        }
    }

    /** Open track as KML in google earth */
    static void openTrackKml(@NonNull Context context, String urlKml) {
        try {
            openTrackGoogleEarth(context, urlKml);
        } catch(ActivityNotFoundException e) {
            Log.e(TAG, "Failed to open KML file in google maps", e);
            Toast.makeText(context, R.string.error_map_intent, Toast.LENGTH_SHORT).show();
        }
    }

    private static void openTrackGoogleEarth(@NonNull Context context, String urlKml) {
        final Intent earthIntent = new Intent(android.content.Intent.ACTION_VIEW);
        earthIntent.setDataAndType(Uri.parse(urlKml), "application/vnd.google-earth.kml+xml");
        earthIntent.setClassName("com.google.earth", "com.google.earth.EarthActivity");
        context.startActivity(earthIntent);
    }

//    /** Share link to base-line.ws */
//    public static void shareTrack(@NonNull Context context, TrackFile trackFile) {
//        final CloudData cloudData = trackFile.getCloudData();
//        if(cloudData != null) {
//            final SimpleDateFormat format = new SimpleDateFormat("EEE MMM d yyyy, h:mma z", Locale.US);
//            final String date = format.format(trackFile.getDate());
//
//            final Intent intent = new Intent();
//            intent.setAction(Intent.ACTION_SEND);
//            intent.putExtra(Intent.EXTRA_SUBJECT, "BASEline Track " + date);
//            intent.putExtra(Intent.EXTRA_TEXT, trackFile.getCloudData().trackUrl);
//            intent.setType("text/plain");
//            context.startActivity(Intent.createChooser(intent, "Share Track"));
//        } else {
//            Log.e("Intents", "Cannot share track because not synced");
//        }
//    }

    /** Share track data file */
    static void exportTrackFile(@NonNull Context context, TrackFile trackFile) {
        try {
            final Uri trackFileUri = FileProvider.getUriForFile(context, "com.platypii.baseline.provider", trackFile.file);
            Log.d(TAG, "Exporting track file " + trackFileUri);
            final Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, trackFile.getName());
            intent.putExtra(Intent.EXTRA_STREAM, trackFileUri);
            intent.setType("application/csv");
            context.startActivity(intent);
        } catch(Exception e) {
            Log.e(TAG, "Failed to export track file", e);
            Toast.makeText(context, R.string.error_export_intent, Toast.LENGTH_SHORT).show();
            FirebaseCrash.report(e);
        }
    }
}
