package com.platypii.baseline;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.platypii.baseline.data.CloudData;
import com.platypii.baseline.data.Jump;
import com.platypii.baseline.data.TheCloud;

public class Intents {

    /** Open jump activity */
    public static void openJumpActivity(Context context, Jump jump) {
        final Intent intent = new Intent(context, JumpActivity.class);
        intent.putExtra("JUMP_FILE", jump.logFile.getName());
        context.startActivity(intent);
    }

    public static void openTrackUrl(final Context context, final CloudData cloudData) {
        // Open cloud url in browser
        final String url = cloudData.trackUrl;
        Log.i("Jumps", "Track already synced, opening " + url);
        final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

    public static void openTrackKml(Context context, final CloudData cloudData) {
        // Open KML intent
        Intent earthIntent = new Intent(android.content.Intent.ACTION_VIEW);
        earthIntent.setDataAndType(Uri.parse(cloudData.trackKml), "application/vnd.google-earth.kml+xml");
        earthIntent.setClassName("com.google.earth", "com.google.earth.EarthActivity");
        context.startActivity(earthIntent);
    }

    // Share jump log using android share options
    public static void shareTrack(Context context, Jump jump) {
        final Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(jump.logFile));
        intent.setType("text/plain");
        context.startActivity(intent);
    }
}
