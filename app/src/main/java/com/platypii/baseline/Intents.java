package com.platypii.baseline;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.platypii.baseline.data.CloudData;
import com.platypii.baseline.data.Jump;

import java.text.SimpleDateFormat;

class Intents {

    /** Open jump activity */
    public static void openJumpActivity(Context context, Jump jump) {
        final Intent intent = new Intent(context, JumpActivity.class);
        intent.putExtra("JUMP_FILE", jump.logFile.getName());
        context.startActivity(intent);
    }

    /** Open track url in browser */
    public static void openTrackUrl(final Context context, final CloudData cloudData) {
        final String url = cloudData.trackUrl;
        Log.i("Jumps", "Track already synced, opening " + url);
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

    /** Open track as KML */
    public static void openTrackKml(Context context, final CloudData cloudData) {
        Intent earthIntent = new Intent(android.content.Intent.ACTION_VIEW);
        earthIntent.setDataAndType(Uri.parse(cloudData.trackKml), "application/vnd.google-earth.kml+xml");
        earthIntent.setClassName("com.google.earth", "com.google.earth.EarthActivity");
        context.startActivity(earthIntent);
    }

    /** Share jump log using android share options */
    public static void shareTrack(Context context, Jump jump) {
        final CloudData cloudData = jump.getCloudData();
        if(cloudData != null) {
            final SimpleDateFormat format = new SimpleDateFormat("EEE MMM d yyyy, h:mma z");
            final String date = format.format(jump.getDate());

            final Intent intent = new Intent();
            intent.setAction(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, "BASEline Track " + date);
            intent.putExtra(Intent.EXTRA_TEXT, jump.getCloudData().trackUrl);
            intent.setType("text/plain");
            context.startActivity(Intent.createChooser(intent, "Share Track"));
        } else {
            Log.e("Intents", "Cannot share track because not synced");
        }
    }
//    public static void shareTrackFile(Context context, Jump jump) {
//        final Intent intent = new Intent();
//        intent.setAction(Intent.ACTION_SEND);
//        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(jump.logFile));
//        intent.setType("text/plain");
//        context.startActivity(intent);
//    }
}
