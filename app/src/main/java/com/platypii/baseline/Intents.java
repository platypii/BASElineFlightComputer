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

    /** Open jump activity */
    public static void openTrackLocal(@NonNull Context context, @NonNull TrackFile trackFile) {
        final Intent intent = new Intent(context, TrackLocalActivity.class);
        intent.putExtra(TrackLocalActivity.EXTRA_TRACK_FILE, trackFile.file.getName());
        context.startActivity(intent);
    }
    public static void openTrackRemote(@NonNull Context context, @NonNull CloudData track) {
        final Intent intent = new Intent(context, TrackRemoteActivity.class);
        intent.putExtra(TrackRemoteActivity.EXTRA_TRACK_ID, track.track_id);
        context.startActivity(intent);
    }

    /** Open track url in browser */
    static void openTrackUrl(@NonNull Context context, @NonNull String url) {
        // Add mobile flag
        if(!url.contains("?")) {
            url += "?mobile";
        }
        Log.i(TAG, "Opening track url " + url);
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }

    /** Open track as KML in google earth */
    static void openTrackKml(@NonNull Context context, String urlKml) {
        try {
            openTrackGoogleEarth(context, urlKml);
        } catch(ActivityNotFoundException e) {
            Log.e(TAG, "Failed to open KML file in google earth", e);
            Toast.makeText(context, R.string.error_map_intent, Toast.LENGTH_SHORT).show();
        } catch(Exception e) {
            FirebaseCrash.report(e);
            Log.e(TAG, "Failed to open KML file in google earth", e);
            Toast.makeText(context, R.string.error_map_intent, Toast.LENGTH_SHORT).show();
        }
    }

    private static void openTrackGoogleEarth(@NonNull Context context, String urlKml) {
        final Intent earthIntent = new Intent(android.content.Intent.ACTION_VIEW);
        earthIntent.setDataAndType(Uri.parse(urlKml), "application/vnd.google-earth.kml+xml");
        earthIntent.setClassName("com.google.earth", "com.google.earth.EarthActivity");
        context.startActivity(earthIntent);
    }

//    /** Share link to baseline.ws */
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
    static void exportTrackFile(@NonNull Context context, @NonNull TrackFile trackFile) {
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

    /** Open help page in browser */
    static void openHelpUrl(@NonNull Context context) {
        final Uri uri = Uri.parse("https://baseline.ws/help");
        Log.i(TAG, "Opening help url " + uri);
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }

}
