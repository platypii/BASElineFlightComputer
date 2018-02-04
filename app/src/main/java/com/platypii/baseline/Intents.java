package com.platypii.baseline;

import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.tracks.TrackFile;
import com.platypii.baseline.util.Exceptions;
import com.platypii.baseline.views.tracks.ChartsActivity;
import com.platypii.baseline.views.tracks.TrackDownloadActivity;
import com.platypii.baseline.views.tracks.TrackLocalActivity;
import com.platypii.baseline.views.tracks.TrackRemoteActivity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;
import java.io.File;

public class Intents {
    private static final String TAG = "Intents";

    /** Open track activity */
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

    /** Open track charts */
    public static void openCharts(@NonNull Context context, @NonNull File trackFile) {
        final Intent intent = new Intent(context, ChartsActivity.class);
        intent.putExtra(TrackLocalActivity.EXTRA_TRACK_FILE, trackFile.getAbsolutePath());
        context.startActivity(intent);
    }
    public static void openCharts(@NonNull Context context, @NonNull CloudData track) {
        // Check if track data file exists
        final File trackFile = track.localFile(context);
        if (trackFile.exists()) {
            // File exists, open charts activity directly
            openCharts(context, trackFile);
        } else {
            // File not downloaded to device, start TrackDownloadActivity
            final Intent intent = new Intent(context, TrackDownloadActivity.class);
            intent.putExtra(TrackDownloadActivity.EXTRA_TRACK_ID, track.track_id);
            context.startActivity(intent);
        }
    }

    /** Open track url in browser */
    public static void openTrackUrl(@NonNull Context context, @NonNull String url) {
        // Add mobile flag
        if(!url.contains("?")) {
            url += "?mobile";
        }
        Log.i(TAG, "Opening track url " + url);
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }

    /** Open track as KML in google earth */
    public static void openTrackKml(@NonNull Context context, String urlKml) {
        try {
            openTrackGoogleEarth(context, urlKml);
        } catch(ActivityNotFoundException e) {
            Log.e(TAG, "Failed to open KML file in google earth", e);
            Toast.makeText(context, R.string.error_map_intent, Toast.LENGTH_SHORT).show();
        } catch(Exception e) {
            Exceptions.report(e);
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
    public static void exportTrackFile(@NonNull Context context, @NonNull TrackFile trackFile) {
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
            Exceptions.report(e);
        }
    }

    /** Open help page in browser */
    public static void openHelpUrl(@NonNull Context context) {
        final Uri uri = Uri.parse("https://baseline.ws/help/app");
        Log.i(TAG, "Opening help url " + uri);
        final Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }

    public static void openBluetoothSettings(@NonNull Context context) {
        try {
            final Intent intent = new Intent(Intent.ACTION_MAIN, null);
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            final ComponentName cn = new ComponentName("com.android.settings", "com.android.settings.bluetooth.BluetoothSettings");
            intent.setComponent(cn);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch(Exception e) {
            Log.e(TAG, "Failed to open android bluetooth settings", e);
            Toast.makeText(context, R.string.error_export_intent, Toast.LENGTH_SHORT).show();
            Exceptions.report(e);
        }
    }
}
