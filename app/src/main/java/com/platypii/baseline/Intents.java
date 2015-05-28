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

    public static void openJump(Context context, Jump jump) {
        // Check if synced or not
        final CloudData cloudData = jump.getCloudData();
        if(cloudData != null) {
            // Open cloud url in browser
            final String url = cloudData.trackUrl;
            Log.i("Jumps", "Track already synced, opening " + url);
            final Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(browserIntent);
        } else {
            Log.i("Jumps", "Track not synced, uploading...");
            Toast.makeText(context, "Syncing track...", Toast.LENGTH_LONG).show();
            // Try to start sync
            TheCloud.uploadAsync(jump);
        }
    }

    public static void openKml(Context context, Jump jump) {
        // Open KML intent
        Intent earthIntent = new Intent(android.content.Intent.ACTION_VIEW);
        earthIntent.setDataAndType(Uri.parse(jump.getCloudData().trackKml), "application/vnd.google-earth.kml+xml");
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
