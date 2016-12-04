package com.platypii.baseline;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.crash.FirebaseCrash;
import com.platypii.baseline.cloud.CloudData;
import com.platypii.baseline.data.Jump;

class Intents {
    private static final String TAG = "Intents";

    /** Open jump activity */
    static void openJumpActivity(@NonNull Context context, Jump jump) {
        final Intent intent = new Intent(context, JumpActivity.class);
        intent.putExtra("JUMP_FILE", jump.logFile.getName());
        context.startActivity(intent);
    }

    /** Open track url in browser */
    static void openTrackUrl(@NonNull Context context, CloudData cloudData) {
        final String url = cloudData.trackUrl;
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

    /** Open track as KML in google earth */
    static void openTrackKml(@NonNull Context context, CloudData cloudData) {
        try {
            openTrackGoogleEarth(context, cloudData);
        } catch(ActivityNotFoundException e) {
            Log.e(TAG, "Failed to open KML file in google maps", e);
            Toast.makeText(context, R.string.error_map_intent, Toast.LENGTH_SHORT).show();
        }
    }

    private static void openTrackGoogleEarth(@NonNull Context context, CloudData cloudData) {
        final Intent earthIntent = new Intent(android.content.Intent.ACTION_VIEW);
        earthIntent.setDataAndType(Uri.parse(cloudData.trackKml), "application/vnd.google-earth.kml+xml");
        earthIntent.setClassName("com.google.earth", "com.google.earth.EarthActivity");
        context.startActivity(earthIntent);
    }

//    /** Share link to base-line.ws */
//    public static void shareTrack(@NonNull Context context, Jump jump) {
//        final CloudData cloudData = jump.getCloudData();
//        if(cloudData != null) {
//            final SimpleDateFormat format = new SimpleDateFormat("EEE MMM d yyyy, h:mma z", Locale.US);
//            final String date = format.format(jump.getDate());
//
//            final Intent intent = new Intent();
//            intent.setAction(Intent.ACTION_SEND);
//            intent.putExtra(Intent.EXTRA_SUBJECT, "BASEline Track " + date);
//            intent.putExtra(Intent.EXTRA_TEXT, jump.getCloudData().trackUrl);
//            intent.setType("text/plain");
//            context.startActivity(Intent.createChooser(intent, "Share Track"));
//        } else {
//            Log.e("Intents", "Cannot share track because not synced");
//        }
//    }

    /** Share track data file */
    static void exportTrackFile(@NonNull Context context, Jump jump) {
        try {
            final Uri trackFileUri = FileProvider.getUriForFile(context, "com.platypii.baseline.provider", jump.logFile);
            Log.d(TAG, "Exporting track file " + trackFileUri);
            final Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, jump.getName());
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
